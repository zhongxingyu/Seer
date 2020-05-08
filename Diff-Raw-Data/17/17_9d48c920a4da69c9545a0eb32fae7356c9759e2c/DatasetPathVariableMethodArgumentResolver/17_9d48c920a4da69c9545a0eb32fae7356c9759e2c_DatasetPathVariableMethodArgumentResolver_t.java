 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.dataset.rest.resolvers;
 
 import static edu.dfci.cccb.mev.dataset.domain.contract.Dataset.VALID_DATASET_NAME_REGEX;
 
 import java.lang.reflect.Method;
 
 import javax.inject.Inject;
 
 import lombok.Getter;
 import lombok.Setter;
 import lombok.ToString;
 
 import org.springframework.web.context.request.NativeWebRequest;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.Workspace;
 
 /**
  * @author levk
  * 
  */
 @ToString (exclude = "workspace")
 public class DatasetPathVariableMethodArgumentResolver extends AbstractTypedPathVariableMethodArgumentResolver<Dataset> {
 
   public static final String DATASET_MAPPING_NAME = "dataset";
   public static final String DATASET_URL_ELEMENT = "{" + DATASET_MAPPING_NAME + ":"
                                                    + VALID_DATASET_NAME_REGEX + "}";
 
   private @Getter @Setter (onMethod = @_ (@Inject)) Workspace workspace;
 
   /**
    * 
    */
   public DatasetPathVariableMethodArgumentResolver () {
     super (Dataset.class, DATASET_MAPPING_NAME);
     setOrder (getOrder () - 1);
   }
 
   /* (non-Javadoc)
    * @see edu.dfci.cccb.mev.dataset.rest.resolvers.
    * AbstractTypedPathVariableMethodArgumentResolver
    * #resolveValue(java.lang.String, org.springframework.core.MethodParameter,
    * org.springframework.web.context.request.NativeWebRequest) */
   @Override
   public Dataset resolveObject (String value, Method method, NativeWebRequest request) throws Exception {
    return value!=null ? workspace.get (value) : null ;
   }
 
   /* (non-Javadoc)
    * @see edu.dfci.cccb.mev.dataset.rest.resolvers.
    * AbstractTypedPathVariableMethodArgumentResolver
    * #resolveValue(java.lang.String,
    * org.springframework.web.context.request.NativeWebRequest) */
   @Override
   public Dataset resolveObject (String value, NativeWebRequest request) throws Exception {
     return resolveObject (value, null, request);
   }
 }
