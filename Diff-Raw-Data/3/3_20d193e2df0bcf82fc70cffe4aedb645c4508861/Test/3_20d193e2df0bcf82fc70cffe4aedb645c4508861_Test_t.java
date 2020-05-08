 /**
  *
  * @author steff
  */
 public class Test {
 
     public static void main(String[] args) {
     
         Box boxA = new Box('#', '_', 3, 4);
         Box boxB = new Box('X', 'o', 3, 2);
         DarkBox darkBoxA = new DarkBox('d', 5, 3);
         ClearBox clearBoxA = new ClearBox(4, 4);
         ClearBox clearBoxB = new ClearBox(2,2);
         FreeBox freeBoxA = new FreeBox("abc\ndef");
         
         Box[][] boxarr=new Box[2][1];
         boxarr[0][0]=new Box('C', '.', 3, 5);
         boxarr[1][0]=new Box('E', 'O', 3, 5);        
         Repeated<Box> reA=new Repeated<Box>(boxarr,1.0);
        Scaled<Box> scA=new Scaled<Box>(boxarr);
         
         System.out.println(boxA);
         System.out.println(boxB);
         System.out.println(darkBoxA);
         System.out.println(clearBoxA);
         System.out.println(clearBoxB);
         System.out.println(freeBoxA);
        System.out.println(boxarr);
     }
 }
