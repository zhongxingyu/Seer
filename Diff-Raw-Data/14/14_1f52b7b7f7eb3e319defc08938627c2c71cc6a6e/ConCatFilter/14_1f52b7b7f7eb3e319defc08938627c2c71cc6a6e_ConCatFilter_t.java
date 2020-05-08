 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 /**
  *
  */
 package org.Transformer.dataset;
 
 import java.util.Map;
 
 import org.Transformer.JobUtils;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class ConCatFilter extends DataFilter
 {
     private String[] FieldThatMustBeEqual;
     private String[] FieldsThatWillBeConCatenated;
    private String conCatSeparator = "";
 
     /**
      *
      */
     public ConCatFilter()
     {
     }
 
     public void setFieldThatMustBeEqual(String[] fieldThatMustBeEqual)
     {
         FieldThatMustBeEqual = fieldThatMustBeEqual;
     }
 
     public void setFieldsThatWillBeConCatenated(String[] fieldsThatWillBeConCatenated)
     {
         FieldsThatWillBeConCatenated = fieldsThatWillBeConCatenated;
     }
 
     public void setConCatSeperator(String conCatSeperator)
     {
        conCatSeparator = conCatSeperator;
     }
 
     /**
      * @see org.Transformer.dataset.DataFilter#applyFilterTo(org.Transformer.dataset.DataSet[])
      */
     @Override
     public DataSet[] applyFilterTo(DataSet[] theData)
     {
         DataSet[] res = new DataSet[1];
         res[0] = new DataSet();
         if((null == FieldThatMustBeEqual) || (null == FieldsThatWillBeConCatenated) || (null == theData))
         {
             return res;
         }
         if(1 > theData.length)
         {
             return res;
         }
         for(int i = 0; i < FieldThatMustBeEqual.length; i++)
         {
             // copy the Fields
             res[0].addDataAtom(theData[0].getDataAtom(FieldThatMustBeEqual[i]), FieldThatMustBeEqual[i]);
         }
         for(int i = 0; i < FieldsThatWillBeConCatenated.length; i++)
         {
             // copy the Fields
             res[0].addDataAtom(theData[0].getDataAtom(FieldsThatWillBeConCatenated[i]), FieldsThatWillBeConCatenated[i]);
         }
         if(2 > theData.length)
         {
             // only one element
             return res;
         }
 
         // check Eqals
         for(int k = 0; k < FieldThatMustBeEqual.length; k++)
         {
             String should = res[0].getDataAtom(FieldThatMustBeEqual[k]);
             if(null != should)
             {
                 for(int i = 1; i < theData.length; i++)
                 {
                     String is = theData[i].getDataAtom(FieldThatMustBeEqual[k]);
                     if(false == should.equals(is))
                     {
                         System.out.println("Checked field is different !(" + should + "|" + is + ")");
                         return null;
                     }
                 }
             }
             else
             {
                 System.out.println("Checked field should Value is null !");
             }
         }
 
         // concat
         for(int k = 0; k < FieldsThatWillBeConCatenated.length; k++)
         {
             String line = "";
             for(int i = 0; i < theData.length; i++)
             {
                line = line + conCatSeparator + theData[i].getDataAtom(FieldsThatWillBeConCatenated[k]);
             }
             res[0].addDataAtom(line, FieldsThatWillBeConCatenated[k]);
         }
 
         return res;
     }
 
     @Override
     public String getConfig()
     {
        return "ConCatSeparator = " + conCatSeparator + "\n"
                + JobUtils.getConfigTextFor(FieldThatMustBeEqual, "FieldThatMustBeEqual")
                + JobUtils.getConfigTextFor(FieldsThatWillBeConCatenated, "FieldsThatWillBeConCatenated");
     }
 
     @Override
     public void setConfig(Map<String, String> cfg)
     {
        conCatSeparator = cfg.get("ConCatSeparator");
         FieldThatMustBeEqual = JobUtils.getStringArrayFromSettingMap(cfg, "FieldThatMustBeEqual");
         FieldsThatWillBeConCatenated = JobUtils.getStringArrayFromSettingMap(cfg, "FieldsThatWillBeConCatenated");
     }
 
     @Override
     public String getName()
     {
         return "ConCatFilter";
     }
 
 }
