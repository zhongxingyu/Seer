 package dataHistographs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class WritableData implements HistData
 {
     ArrayList<Float> data;
     
     public WritableData()
     {
         data = new ArrayList<Float>();
     }
     public WritableData(int len)
     {
         data = new ArrayList<Float>(len);
     }
    public WritableData(float[] initDat)
    {
        data = new ArrayList<Float>(initDat.length);
        for(int i=0; i<initDat.length; i++)
        {
            data.add(initDat[i]);
        }
    }
    public WritableData(float[] initDat)
     {
         data = new ArrayList<Float>(initDat.length);
         for(int i=0; i<initDat.length; i++)
         {
             data.add(initDat[i]);
         }
     }
     
     public void addNext(float f)
     {
         data.add(f);
     }
     public void addNextFrom(HistData d)
     {
         int insertIndex = data.size();
         data.add(d.getCO2(insertIndex));
     }
     public float getCO2(int i)
     {
         return data.get(i);
     }
     public float[] getAllCO2()
     {
         float[] returnMe = new float[data.size()];
         for(int i=0; i<this.size(); i++)
         {
             returnMe[i] = this.getCO2(i);
         }
         return returnMe;
     }
     public int size()
     {
         return data.size();
     }
     public boolean equals(Object other)
     {
         if (other instanceof HistData)
         {
             HistData o = (HistData) other;
             if(size() != o.size())
                 return false;
             
             boolean good = true;
             for (int i=0; i<size(); i++)
             {
             	float thisDat = getCO2(i);
     			float otherDat = o.getCO2(i);
             	good = good && (thisDat==otherDat);
             }
         }
         return false;
     }
     public int hashCode()
     {
         return data.hashCode();
     }
 }
