 /*
    Author        :  LiuJiang
    Compile Date  :  2013/10/21
    Introduction  :
      Homework    :  Ex2.3
      Give out the VAMPIRE NUMBER between 1000 and 9999.
 */
 public class EX2_3_Main {
 
  
 static class TNUM
 {
 	int x=0,a=0,b=0;
 }
 
 
 static class TChecker
 {
   TNUM[] num=new TNUM[10];
   int x,a,b;
   TChecker(int x,int a,int b)
   {
 	  this.x=x;this.a=a;this.b=b;
 	  for (int i=0;i<10;i++)
 		 num[i]=new TNUM();
   }
   private void Divide(int t,int type)
   {
 	  //type 1:divide x;
 	  //type 2:divide a;
 	  //type 3:divide b;
 	  do
 	  {
 		  int s=t%10;
 		  switch (type)
 		  {
 		  case 1:
 			   //is x;
 			   num[s].x++;
 			   break;
 		  case 2:
 			   //is a;
 			   num[s].a++;
 			   break;
 		  case 3:
 			   //is b;
 			   num[s].b++;
 			   break;
 		  }
 		  t/=10;
 	  }
 	  while (t!=0);
 	  
   }
   public boolean DoCheck()
   {
 	  Divide(x,1);Divide(a,2);Divide(b,3);
 	  /*FOR DEBUG....
 	   * for (int i=0;i<=9;i++)
 		    System.out.println("nums "+i+" x:"+num[i].x+" a:"+num[i].a+" b:"+num[i].b);
 	  */
 	  boolean flag=true;
 	  for (int i=0;i<=9;i++)
 		  if (num[i].x!=(num[i].a+num[i].b))
 		  {
 			  flag=false;
 			  break;
 		  }
 	 return (flag && x<=9999 && a<b);
   }
 }
   public static void main(String[] args)
    {
 
 	   for (int i=10;i<99;i++)
		   for (int j=1000/i;j<9999/10;j++)
 		   {
 			   int x=i*j;
 			   TChecker Check=new TChecker(x,i,j);
 			   if (Check.DoCheck())
 				   System.out.print(x+" ");
 		   }
       
        }
   
 }
