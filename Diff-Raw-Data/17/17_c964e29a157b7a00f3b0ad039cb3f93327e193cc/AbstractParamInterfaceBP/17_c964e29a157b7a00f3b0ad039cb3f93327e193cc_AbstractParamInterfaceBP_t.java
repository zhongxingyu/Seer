 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.Locale;
 
 import org.eclipse.jubula.client.core.model.IDataSetPO;
 import org.eclipse.jubula.client.core.model.IModifiableParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.ComboParamValidator;
 import org.eclipse.jubula.client.core.utils.GuiParamValueConverter;
 import org.eclipse.jubula.client.core.utils.IParamValueValidator;
 import org.eclipse.jubula.client.core.utils.IntegerParamValueValidator;
 import org.eclipse.jubula.client.core.utils.ModelParamValueConverter;
 import org.eclipse.jubula.client.core.utils.NullValidator;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter;
 import org.eclipse.jubula.client.core.utils.VariableParamValueValidator;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 13, 2010
  * @param <T>
  *            parameterized type
  */
 public abstract class AbstractParamInterfaceBP<T> {
     /**
      * Adds a new data set into the given row of the given IParameterInterface
      * 
      * @param obj
      *            the IParameterInterface object
      * @param row
      *            the row to insert
      */
     public void addDataSet(IParameterInterfacePO obj, int row) {
         obj.getDataManager().insertDataSet(row);
     }
     
     /**
      * Removes a test data row in the test data manager of the passed node.
      * 
      * @param paramNode
      *            The parameter node
      * @param row
      *            The row to remove
      * @param mapper
      *            mapper to resolve param names
      * @param locale
      *            currently used language
      */
     public void removeDataSet(IParameterInterfacePO paramNode, int row,
             IParamNameMapper mapper, Locale locale) {
 
         int colCount = paramNode.getDataManager().getColumnCount();
         for (int i = 0; i < colCount; i++) {
             final String uniqueId = paramNode.getDataManager().getUniqueIds()
                     .get(i);
             final IParamDescriptionPO desc = paramNode
                     .getParameterForUniqueId(uniqueId);
             if (desc != null) {
                 GuiParamValueConverter conv = new GuiParamValueConverter(null,
                         paramNode, locale, desc, AbstractParamInterfaceBP
                                 .createParamValueValidator(
                                         desc.getType(), false));
                 startParameterUpdate(conv, locale, row, mapper);
             }
         }
         paramNode.getDataManager().removeDataSet(row);
     }
     
     /**
      * Updates the specified cell (row, column) in the test data manager of the
      * passed parameter object.
      * 
      * @param conv
      *            converter contains value to update
      * @param locale
      *            the locale of the test data
      * @param row
      *            The test data row
      * @param mapper
      *            mapper to resolve param names
      */
     public void startParameterUpdate(GuiParamValueConverter conv,
             Locale locale, int row, IParamNameMapper mapper)  {
         IParameterInterfacePO paramNode = conv.getCurrentNode();
         IParamDescriptionPO paramDescription = conv.getDesc();
         String paramGuid = paramDescription.getUniqueId();
         if (paramNode.getParameterList().contains(conv.getDesc()) 
                 && !paramNode.getDataManager().getUniqueIds().contains(
                         paramGuid)) {
             // This prevents the scenario where a (new) parameter exists in 
             // the node's parameter list, but not in its data manager.
             paramNode.getDataManager().addUniqueId(paramGuid);
         }
         try {
             // do nothing, if parameter value is unchanged
             ITestDataPO data = 
                 paramNode.getDataManager().getCell(row, paramDescription);
             String value = data.getValue(locale);
             final String modelString = conv.getModelString();
             if (modelString != null && modelString.equals(value)) {
                 return;
             }
         } catch (IndexOutOfBoundsException e) { // NOPMD
             // nothing
         }
         updateParam(conv, locale, mapper, row);
     }
 
     /**
      * get the gui representation for parameter value of given param description for first dataset
      * @param node current node
      * @param desc param description belonging to searched param value
      * @param rowCount datasetNumber - 1
      * @param locale currently used language
      * @return gui representation of parameter value for given parameter description
      */
     public static String getGuiStringForParamValue(
             final IParameterInterfacePO node, final IParamDescriptionPO desc, 
             int rowCount, Locale locale) {
 
         String result = StringConstants.EMPTY;
         IParameterInterfacePO srcNode = node;
         IParamDescriptionPO srcDesc = desc;
         while (srcNode.getReferencedDataCube() != null) {
             srcNode = srcNode.getReferencedDataCube();
             srcDesc = srcNode.getParameterForName(srcDesc.getName());
 
             // Existence and type compatibility check
             if (srcDesc == null || !desc.getType().equals(srcDesc.getType())) {
                 return result;
             }
         }
         if (srcDesc == null) {
             // Parameter is not present in the referenced data source.
             // Return empty test data.
             return result;
         }
         int col = srcNode.getDataManager().findColumnForParam(
                 srcDesc.getUniqueId());
         if (col > -1 && srcNode.getDataManager().getDataSetCount() > rowCount) {
             IDataSetPO row = srcNode.getDataManager().getDataSet(rowCount);
             try {
                 ITestDataPO td = row.getColumn(col);
                 ParamValueConverter conv = 
                     new ModelParamValueConverter(td.getValue(locale),
                             srcNode, locale, srcDesc);
                 result = conv.getGuiString();
             } catch (IndexOutOfBoundsException e) {
                 // do nothing
             }
         }
         return result;
     }
     
     /**
      * adds new parameter(s) to the parent, if the current node contains new 
      * references. This method doesn't validate, if it's allowed to change the interface.
      * This validation has to run before.
      * Updates the parametervalue of current node in model
      * hint: if the user has removed a reference in current parameter value, the 
      * corresponding parameter of parent won't be deleted. A removal of parameters
      * is only allowed, when the user calls the change parameters dialog.
      * 
      * @param conv converter containing parameter value to update
      * @param locale the locale of the test data
      * description will be removed
      * @param mapper mapper to resolve param names will be added
      * @param row current dataset number
      */
     protected abstract void updateParam(GuiParamValueConverter conv,
             Locale locale, IParamNameMapper mapper, int row);
 
     /**
      * Updates the test data manager of the passed node by writing the
      * value contained in converter into the appropriate cell.
      * 
      * @param conv converter contains parameter value to write
      * @param locale the locale of the test data
      * @param dataSetRow The row of the test data manager
      */
    protected void writeTestDataEntry(GuiParamValueConverter conv,
         Locale locale, int dataSetRow) {
 
         ITestDataPO oldTd = null;
         final IParamDescriptionPO desc = conv.getDesc();
         try {
             oldTd = conv.getCurrentNode().getDataManager().getCell(dataSetRow,
                 desc);
         } catch (IndexOutOfBoundsException e) { // NOPMD by al on 3/19/07 1:23 PM
             // Nothing to be done
         }
         ITestDataPO td = createOrUpdateTestDataPO(oldTd, conv, locale);
         conv.getCurrentNode().getDataManager().updateCell(td, dataSetRow,
             desc.getUniqueId());
     }
     
     
     /**
      * Creates a new test data instance, if the passed test data is
      * <code>null</code>, or updates the passed one with the given value.
      * 
      * @param testData The existing test data or <code>null</code>
      * @param conv converter with value to update
      * @param locale the locale of the test data.
      * @return The (new) test data instance.
      *             If the creation of the Test Data fails
      */
     private ITestDataPO createOrUpdateTestDataPO(ITestDataPO testData,
        GuiParamValueConverter conv, Locale locale) {

         ITestDataPO td = null;
         if (testData != null) {
             td = testData;
         } else {
             td = TestDataBP.instance().createEmptyTestData();            
         }

        // A new converter is instantiated and used here in order to cover
        // the corner case described in bug 370718.
        GuiParamValueConverter newConv = new GuiParamValueConverter(
                conv.getGuiString(), conv.getCurrentNode(), locale, 
                conv.getDesc(), 
                AbstractParamInterfaceBP.createParamValueValidator(
                        TestDataConstants.STR, false));
        td.setValue(locale, newConv.getModelString(), 
             GeneralStorage.getInstance().getProject());
         return td;
     }
     
     /**
      * @param name
      *            the new name of the parameter
      * @param type
      *            the type of the parameter
      * @param obj
      *            the object to add the parameter for
      * @param mapper
      *            the mapper to resolve param names
      */
     public void addParameter(String name, String type,
             IModifiableParameterInterfacePO obj, IParamNameMapper mapper) {
         obj.addParameter(type, name, mapper);
     }
 
     /**
      * @param desc
      *            the param to remove
      * @param paramIntObj
      *            the object to remove the param from
      * @param locale
      *            the locale to use
      */
     public abstract void removeParameter(IParamDescriptionPO desc,
             T paramIntObj, Locale locale);
 
     /**
      * @param desc
      *            the param to rename
      * @param newName
      *            the new name
      * @param mapper
      *            the mapper to use
      */
     public void renameParameters(IParamDescriptionPO desc,
             String newName, ParamNameBPDecorator mapper) {
         mapper.addNameToUpdate(desc.getUniqueId(), newName);
     }
     
     /**
      * @param type type of parameter
      * @param valuesAreCombinable
      *            whether combinations of the supplied values are allowed
      * @param values list of possible values for a parameter
      * @return validator fit to given type
      */
     public static IParamValueValidator createParamValueValidator(
         String type, boolean valuesAreCombinable, String... values) {
         if (TestDataConstants.INTEGER.equals(type)) {
             return new IntegerParamValueValidator(Integer.MIN_VALUE, 
                 Integer.MAX_VALUE, values);
         }
         if (TestDataConstants.VARIABLE.equals(type)) {
             return new VariableParamValueValidator();
         }
         if (TestDataConstants.STR.equals(type)) {
             return new NullValidator();
         }
         if (TestDataConstants.COMBO.equals(type)
             || TestDataConstants.BOOLEAN.equals(type)) {
             return new ComboParamValidator(values, valuesAreCombinable);
         }
         return new NullValidator();
     }
 }
