 class SC {
 void m(boolean b) {
     try {
         if (b) return;
     } finally {
         b = false;
     }
 }
 }
