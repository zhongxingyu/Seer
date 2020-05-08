 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.ui.validators;
 
 import org.amanzi.neo.loader.core.CommonConfigData;
 import org.amanzi.neo.loader.core.ILoaderInputValidator;
 import org.amanzi.neo.loader.core.IValidateResult;
 import org.amanzi.neo.loader.core.IValidateResult.Result;
 import org.amanzi.neo.loader.core.ValidateResultImpl;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.neo4j.graphdb.Node;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author NiCK
  * @since 1.0.0
  */
 public class ExistNetworkValidator implements ILoaderInputValidator<CommonConfigData> {
 
     @Override
     public void filter(CommonConfigData data) {
     }
 
     @Override
     public IValidateResult validate(final CommonConfigData data) {
         if (data.getProjectName() == null || data.getDbRootName() == null)
             return new ValidateResultImpl(Result.FAIL, "Network is not found.");
         DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
         Node root = datasetService.findRoot(data.getProjectName(), data.getDbRootName());
         if (root == null || datasetService.getNodeType(root) != NodeTypes.NETWORK) {
             return new ValidateResultImpl(Result.FAIL, String.format("Network '%s' is not found. ", data.getDbRootName()) + "For loader '%s' network should exist.");
         }
         return new ValidateResultImpl(Result.SUCCESS, "");
     }
 
     @Override
     public IValidateResult accept(CommonConfigData data) {
        return new ValidateResultImpl(Result.UNKNOWN, "");
     }
 
 }
