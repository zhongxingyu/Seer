 package ch.jmnetwork.cookieclicker;
 
 public class CookieManager
 {
     private long cookiesTotal = 0, cookiesCurrent = 0, cookiesHandmade = 0;
     public float decimalValue;
     
    public void addHandmadeCookies(int cookiesAmount)
     {
        cookiesHandmade += cookiesAmount;
        addCookies(cookiesAmount);
     }
     
     public long getHandmadeCookies()
     {
         return cookiesHandmade;
     }
     
     public void addCookies(int cookiesAmount)
     {
         if (cookiesCurrent + cookiesAmount < Long.MAX_VALUE)
         {
             cookiesTotal += cookiesAmount;
             cookiesCurrent += cookiesAmount;
         }
     }
     
     public long getCurrentCookies()
     {
         return cookiesCurrent;
     }
     
     public boolean buyPrice(long cookiesPrice)
     {
         if (cookiesCurrent >= cookiesPrice)
         {
             cookiesCurrent -= cookiesPrice;
             return true;
         }
         else
         {
             return false;
         }
     }
     
     public long getTotalCookies()
     {
         return cookiesTotal;
     }
     
     /**
      * PLEASE ONLY USE FOR SAVE / LOAD PROCESS
      * 
      * @param cookiestotal
      */
     public void setTotalCookies(long cookiestotal)
     {
         this.cookiesTotal = cookiestotal;
     }
     
     /**
      * PLEASE ONLY USE FOR SAVE / LOAD PROCESS
      * 
      * @param currentCookies
      */
     public void setCurrentCookies(long currentCookies)
     {
         this.cookiesCurrent = currentCookies;
     }
     
     /**
      * PLEASE ONLY USE FOR SAVE / LOAD PROCESS
      * 
      * @param cookiesTotal
      */
     public void setHandmadeCookies(long cookiesTotal)
     {
         cookiesHandmade = cookiesTotal;
     }
 }
