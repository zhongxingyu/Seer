 
 
 public enum Cities {
     Home,
     Cornell,
     Harvard,
     Yale,
     Boston,
     Falls;
 
     public int distance(Cities other) {
         int dist = -1;
         switch (this) {
             case Home:
                 switch (other) {
                     case Home:
                         dist = 0;
                         break;
                     case Cornell:
                         dist = 397;
                         break;
                     case Harvard:
                         dist = 881;
                         break;
                     case Yale:
                         dist = 820;
                         break;
                     case Boston:
                         dist = 886;
                         break;
                     case Falls:
                         dist = 338;
                         break;
                 }
                 break;
             case Cornell:
                 switch (other) {
                     case Cornell:
                         dist = 0;
                         break;
                     case Harvard:
                         dist = 530;
                         break;
                     case Yale:
                         dist = 419;
                         break;
                     case Boston:
                         dist = 533;
                         break;
                     case Falls:
                         dist = 67;
                         break;
                     default:
                         break;
                 }
                 break;
             case Harvard:
                 switch (other) {
                     case Harvard:
                         dist = 0;
                         break;
                     case Yale:
                         dist = 217;
                         break;
                     case Boston:
                         dist = 5;
                         break;
                     case Falls:
                         dist = 557;
                         break;
                     default:
                         break;
                 }
                 break;
             case Yale:
                 switch (other) {
                     case Yale:
                         dist = 0;
                         break;
                     case Boston:
                         dist = 222;
                         break;
                     case Falls:
                         dist = 502;
                         break;
                 }
                 break;
             case Boston:
                 switch (other) {
                     case Boston:
                         dist = 0;
                         break;
                     case Falls:
                        dist = 502;
                         break;
                     default:
                         break;
                 }
                 break;
             case Falls:
                 switch (other) {
                     case Falls:
                         dist = 0;
                         break;
                     default:
                         break;
                 }
                 break;
         }
         if (dist == -1) return other.distance(this);
         else return dist;
     }
 }
