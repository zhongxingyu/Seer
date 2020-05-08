 class SC {
 void m(boolean b) {
     try {
         if (b) return;
    } catch (Exception e) {
        b = true;
     } finally {
         b = false;
     }
 }
 }
