 class TC {
   void m (boolean b) {
     try {
       if (b) return;
      if (!b) return;
    } catch (Exception e) {
       b = true;
    }
   }
 }
