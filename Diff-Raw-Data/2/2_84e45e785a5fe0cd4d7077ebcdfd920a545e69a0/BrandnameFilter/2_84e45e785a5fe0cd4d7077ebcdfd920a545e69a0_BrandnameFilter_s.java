 package baseline.filter;
 
 import baseline.Dictionary;
 
 public class BrandnameFilter extends MentionFilter{
 
     public BrandnameFilter(Dictionary brandnames) {
         super();
         this.brandnames = brandnames;
     }
 
     Dictionary brandnames;
     
     @Override
     public boolean isValidMention(String[] text, int startIndex, int endIndex) {
         int noBrands = 0;
         for(int i=startIndex; i<=endIndex; i++) {
             if(brandnames.contains(text[i]))
                 noBrands++;
         }
         
         //It must not contain two or more different brand names
         if(noBrands>1)
             return false;
         
         // If 1-token long then it must not be a brand
         if(startIndex==endIndex && noBrands==1)
             return false;
        return false;
     }
 
 }
