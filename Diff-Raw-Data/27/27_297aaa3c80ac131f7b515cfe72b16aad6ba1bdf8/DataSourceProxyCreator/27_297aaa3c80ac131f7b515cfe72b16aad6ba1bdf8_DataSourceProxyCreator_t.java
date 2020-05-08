 /*
  * Copyright 2011 cruxframework.org.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.cruxframework.crux.core.rebind.datasource;
 
 import java.io.PrintWriter;
 
 import org.cruxframework.crux.core.client.Crux;
 import org.cruxframework.crux.core.client.datasource.ColumnDefinition;
 import org.cruxframework.crux.core.client.datasource.ColumnDefinitions;
 import org.cruxframework.crux.core.client.datasource.LocalDataSource;
 import org.cruxframework.crux.core.client.datasource.RemoteDataSource;
 import org.cruxframework.crux.core.client.datasource.annotation.DataSource;
 import org.cruxframework.crux.core.client.datasource.annotation.DataSourceRecordIdentifier;
 import org.cruxframework.crux.core.client.formatter.HasFormatter;
 import org.cruxframework.crux.core.client.screen.ScreenBindableObject;
 import org.cruxframework.crux.core.i18n.MessagesFactory;
 import org.cruxframework.crux.core.rebind.AbstractInvocableProxyCreator;
 import org.cruxframework.crux.core.rebind.CruxGeneratorException;
 import org.cruxframework.crux.core.rebind.GeneratorMessages;
 import org.cruxframework.crux.core.utils.JClassUtils;
 import org.cruxframework.crux.core.utils.RegexpPatterns;
 
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.ext.GeneratorContextExt;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JField;
 import com.google.gwt.core.ext.typeinfo.JPackage;
 import com.google.gwt.core.ext.typeinfo.JType;
 import com.google.gwt.core.ext.typeinfo.NotFoundException;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
 import com.google.gwt.user.rebind.SourceWriter;
 import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 public class DataSourceProxyCreator extends AbstractInvocableProxyCreator
 {
 	protected static GeneratorMessages messages = (GeneratorMessages)MessagesFactory.getMessages(GeneratorMessages.class);
 	private static final String DATASOURCE_PROXY_SUFFIX = "_DataSourceProxy";
 
 	private final JClassType dataSourceClass;
 	private final JClassType dtoType;
 	private final JClassType recordType;
 
 	private final boolean isAutoBindEnabled;
 	private String identifier;
 
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param logger
 	 * @param context
 	 * @param crossDocumentIntf
 	 */
 	public DataSourceProxyCreator(TreeLogger logger, GeneratorContextExt context, JClassType dataSourceClass)
 	{
 		super(logger, context, null, dataSourceClass);
 		this.dataSourceClass = dataSourceClass;
 		this.dtoType = getDtoTypeFromClass();
 		this.recordType = getRecordTypeFromClass();
 		DataSource dsAnnot = dataSourceClass.getAnnotation(DataSource.class);
 		this.isAutoBindEnabled = (dsAnnot == null || dsAnnot.autoBind());
 		this.identifier = getDataSourceIdentifier();
 	}
 	
 	/**
 	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateProxyContructor(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyContructor(SourceWriter srcWriter)
 	{
 		srcWriter.println();
 		srcWriter.println("public " + getProxySimpleName() + "() {");
 		srcWriter.indent();
		generateAutoCreateFields(srcWriter, "this", isAutoBindEnabled);
 		srcWriter.outdent();
 		srcWriter.println("}");
 	}	
 	
 	/**
 	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateProxyFields(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyFields(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateProxyMethods(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyMethods(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 		try
         {
 	        JClassType remoteDsType = dataSourceClass.getOracle().getType(RemoteDataSource.class.getCanonicalName());
 	        JClassType localDsType = dataSourceClass.getOracle().getType(LocalDataSource.class.getCanonicalName());
 	        if (remoteDsType.isAssignableFrom(dataSourceClass))
 	        {
 	        	generateFetchFunction(srcWriter);
 	        }
 	        else if (localDsType.isAssignableFrom(dataSourceClass))
 	        {
 	        	generateLoadFunction(srcWriter);
 	        }
 
 	        generateUpdateFunction(srcWriter);
 	        generateGetBoundObjectFunction(srcWriter);
 
 	        generateScreenUpdateWidgetsFunction(dataSourceClass, srcWriter);
 	        generateControllerUpdateObjectsFunction(dataSourceClass, srcWriter);
 	        generateIsAutoBindEnabledMethod(srcWriter, isAutoBindEnabled);
         }
         catch (NotFoundException e)
         {
         	throw new CruxGeneratorException(e.getMessage(), e);
         }
 	}	
 	
 	/**
 	 * 
 	 * @param sourceWriter
 	 */
 	private void generateGetBoundObjectFunction(SourceWriter sourceWriter)
 	{
 		try
 		{
 			sourceWriter.println("public "+dtoType.getParameterizedQualifiedSourceName()+
 					                   " getBoundObject("+recordType.getParameterizedQualifiedSourceName()+" record){");
 			sourceWriter.indent();
 			sourceWriter.println("if (record == null) return null;");
 			sourceWriter.println("return record.getRecordObject();");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 		}
 		catch (Exception e)
 		{
 			logger.log(TreeLogger.ERROR, messages.errorGeneratingRegisteredDataSource(dataSourceClass.getName(), e.getLocalizedMessage()), e);
 		}
 	}	
 	
 	/**
 	 * 
 	 * @param sourceWriter
 	 */
 	private void generateUpdateFunction(SourceWriter sourceWriter)
 	{
 		try
 		{
 			String recordTypeDeclaration = recordType.getQualifiedSourceName();
 			
 			sourceWriter.println("public void updateData("+dtoType.getParameterizedQualifiedSourceName()+"[] data){");
 			sourceWriter.indent();
 			sourceWriter.println(recordTypeDeclaration+"[] ret = new "+recordTypeDeclaration+"[(data!=null?data.length:0)];");
 			sourceWriter.println("for (int i=0; i<data.length; i++){");
 			sourceWriter.indent();
 			sourceWriter.print("ret[i] = new "+recordType.getParameterizedQualifiedSourceName()+"(this,");
 			sourceWriter.print(getIdentifierDeclaration("data[i]"));
 			sourceWriter.println(");");
 			sourceWriter.println("ret[i].setRecordObject(data[i]);");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 			sourceWriter.println("update(ret);");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 
 			sourceWriter.println("public void updateData(java.util.List<"+dtoType.getParameterizedQualifiedSourceName()+"> data){");
 			sourceWriter.indent();
 			sourceWriter.println(recordTypeDeclaration+"[] ret = new "+recordTypeDeclaration+"[(data!=null?data.size():0)];");
 			sourceWriter.println("for (int i=0; i<data.size(); i++){");
 			sourceWriter.indent();
 			sourceWriter.print("ret[i] = new "+recordType.getParameterizedQualifiedSourceName()+"(this,");
 			sourceWriter.print(getIdentifierDeclaration("data.get(i)"));
 			sourceWriter.println(");");
 			sourceWriter.println("ret[i].setRecordObject(data.get(i));");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 			sourceWriter.println("update(ret);");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 		
 		}
 		catch (Exception e)
 		{
 			logger.log(TreeLogger.ERROR, messages.errorGeneratingRegisteredDataSource(dataSourceClass.getName(), e.getLocalizedMessage()), e);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param parentVariable
 	 * @return
 	 */
 	private String getIdentifierDeclaration(String parentVariable) 
 	{
 		String[] identifier = RegexpPatterns.REGEXP_COMMA.split(this.identifier);
 		StringBuilder result = new StringBuilder("\"\""); 
 
 		for (int i = 0; i < identifier.length; i++)
 		{
 			String[] fields = RegexpPatterns.REGEXP_DOT.split(identifier[i]);
 			if (fields != null)
 			{
 				StringBuilder fieldExpression = new StringBuilder();
 				boolean first = true;
 				JType fieldType = dtoType;
 				for (String fieldName : fields)
                 {
 					JField field = ((JClassType)fieldType).findField(fieldName.trim());
 					if (field == null)
 					{
 						throw new CruxGeneratorException(messages.errorGeneratingRegisteredDataSourceCanNotFindIdentifier(dataSourceClass.getName(), identifier[i]));
 					}
 					if (first)
 					{
 						fieldExpression.append(parentVariable);
 					}
 					first = false;
 					fieldExpression.append(getFieldValueGet((JClassType)fieldType, field, "", false));
 					fieldType = field.getType();
                 }
 				result.append("+"+fieldExpression.toString());
 			}
 		}
 		return result.toString();
 	}	
 	
 	/**
 	 * 
 	 * @param sourceWriter
 	 */
 	private void generateLoadFunction(SourceWriter sourceWriter)
 	{		
 		try
 		{
 			sourceWriter.println("public void load(){");
 			sourceWriter.indent();
 			if (isAutoBindEnabled)
 			{
 				sourceWriter.println("updateControllerObjects();");
 			}
 			sourceWriter.println("super.load();");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 		}
 		catch (Exception e)
 		{
 			logger.log(TreeLogger.ERROR, messages.errorGeneratingRegisteredDataSource(dataSourceClass.getQualifiedSourceName(), e.getLocalizedMessage()), e);
 		}
 	}	
 	
 	/**
 	 * 
 	 * @param sourceWriter
 	 */
 	private void generateFetchFunction(SourceWriter sourceWriter)
 	{
 		try
 		{
 			sourceWriter.println("public void fetch(int startRecord, int endRecord){");
 			sourceWriter.indent();
 			if (isAutoBindEnabled)
 			{
 				sourceWriter.println("updateControllerObjects();");
 			}
 			sourceWriter.println("super.fetch(startRecord, endRecord);");
 			sourceWriter.outdent();
 			sourceWriter.println("}");
 		}
 		catch (Exception e)
 		{
 			logger.log(TreeLogger.ERROR, messages.errorGeneratingRegisteredDataSource(dataSourceClass.getQualifiedSourceName(), e.getLocalizedMessage()), e);
 		}
 	}
 	
 	/**
 	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateTypeSerializers(SerializableTypeOracle, SerializableTypeOracle)
 	 */
 	@Override
 	protected void generateTypeSerializers(SerializableTypeOracle typesSentFromBrowser, SerializableTypeOracle typesSentToBrowser) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * @return
 	 */
 	protected String[] getImports()
     {
 	    String[] imports = new String[] {
     		GWT.class.getCanonicalName(), 
     		org.cruxframework.crux.core.client.screen.Screen.class.getCanonicalName(),
     		HasValue.class.getCanonicalName(),
     		HasText.class.getCanonicalName(),
     		HasFormatter.class.getCanonicalName(),
     		Widget.class.getCanonicalName(),
     		Crux.class.getCanonicalName(), 
     		ColumnDefinition.class.getCanonicalName(), 
     		ColumnDefinitions.class.getCanonicalName() 
 		};
 	    return imports;
     }
 	
 	/**
 	 * @return the full qualified name of the proxy object.
 	 */
 	public String getProxyQualifiedName()
 	{
 		return dataSourceClass.getPackage().getName() + "." + getProxySimpleName();
 	}
 
 	/**
 	 * @return the simple name of the proxy object.
 	 */
 	public String getProxySimpleName()
 	{
 		return dataSourceClass.getSimpleSourceName() + DATASOURCE_PROXY_SUFFIX;
 	}
 	
 	
 	/**
 	 * @return a sourceWriter for the proxy class
 	 */
 	protected SourceWriter getSourceWriter()
 	{
 		JPackage pkg = dataSourceClass.getPackage();
 		String packageName = pkg == null ? "" : pkg.getName();
 		PrintWriter printWriter = context.tryCreate(logger, packageName, getProxySimpleName());
 
 		if (printWriter == null)
 		{
 			return null;
 		}
 
 		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, getProxySimpleName());
 
 		String[] imports = getImports();
 		for (String imp : imports)
 		{
 			composerFactory.addImport(imp);
 		}
 
 		composerFactory.setSuperclass(dataSourceClass.getParameterizedQualifiedSourceName());
 		composerFactory.addImplementedInterface(ScreenBindableObject.class.getCanonicalName());
 
 		return composerFactory.createSourceWriter(context, printWriter);
 	}
 	
 	/**
 	 * @return
 	 */
 	@SuppressWarnings("deprecation")
     private String getDataSourceIdentifier()
     {
 		DataSourceRecordIdentifier idAnnotation = 
 			dataSourceClass.getAnnotation(DataSourceRecordIdentifier.class);
 		if (idAnnotation != null)
 		{
 			return idAnnotation.value();
 		}
 
 		org.cruxframework.crux.core.client.datasource.annotation.DataSourceBinding typeAnnot = 
 			dataSourceClass.getAnnotation(org.cruxframework.crux.core.client.datasource.annotation.DataSourceBinding.class);
 		return typeAnnot.identifier();
     }
 	
 	/**
 	 * 
 	 * @param logger
 	 * @param dataSourceClass
 	 * @return
 	 */
 	private JClassType getDtoTypeFromClass()
 	{
 		return getTypeFromMethodClass("getBoundObject");
 	}
 
 	/**
 	 * @param methodName
 	 * @return
 	 */
 	private JClassType getTypeFromMethodClass(String methodName)
     {
 		JType returnType = JClassUtils.getReturnTypeFromMethodClass(dataSourceClass, methodName, new JType[]{});
 		JClassType returnClassType = returnType.isClassOrInterface();
 		
 		if (returnClassType == null)
 		{
 			throw new CruxGeneratorException(messages.errorGeneratingRegisteredDataSourceInvalidBoundObject(dataSourceClass.getName()));
 		}
 		return returnClassType;
     }
 	
 	/**
 	 * @return
 	 */
 	private JClassType getRecordTypeFromClass()
 	{
 		return getTypeFromMethodClass("getRecord");
 	}
 }
