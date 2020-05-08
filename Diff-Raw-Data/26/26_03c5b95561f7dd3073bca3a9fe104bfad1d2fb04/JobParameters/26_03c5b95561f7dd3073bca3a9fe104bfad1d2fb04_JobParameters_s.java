 /*
  *     This file is part of Metabolic Network Builder
  *
  *     Metabolic Network Builder is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Foobar is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.metabolomes.descriptor.observation;
 
 import java.util.Calendar;
 import java.util.HashMap;
 
 /**
  * JobParameters.java
  *
  *
  * @author johnmay
  * @date May 9, 2011
  */
 public class JobParameters
         extends HashMap<String , Object> {
 
     private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( JobParameters.class );
     private static final long serialVersionUID = -1559314214417094490L;
 
 
     public JobParameters() {
         put( JobParamType.DATE , Calendar.getInstance().getTime() );
        put( JobParamType.JOBID, get( "date" ).toString() );
     }
 
     public JobParameters( String jobid ) {
         put( JobParamType.DATE, Calendar.getInstance().getTime() );
         put( JobParamType.JOBID , jobid );
     }
 
     public String getAsArgument(String jobParamString){
         return " -" + jobParamString + " " + get(jobParamString).toString();
     }
 }
