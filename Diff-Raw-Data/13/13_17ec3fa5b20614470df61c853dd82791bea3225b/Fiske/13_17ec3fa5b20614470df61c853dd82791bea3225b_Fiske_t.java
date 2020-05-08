class Fiske {
 	public static void main(String[] argv){
 		System.out.println(new Fisk().start());
 	}
 }
 class Fisk{
     public int start(){
 		int z;
 		int _k_;
 		int y;
 		int tmp;
 		Matrix m;
 	int[] list;
 	int a;
 		z = 15;
 		y = 3;
 		m = new Matrix();
 	tmp = m.start();
 			list = this.fiske2(m,z-1,y-1);
 			a = 0;
 			while(a<y){
 		System.out.println(list[a]);
 				a = a + 1;
 				}
 				return 0;
 	}
 
     public int[] fiske2(Matrix m,int n,int k)
     {
 		int z;
 		int y;
        int jamfor;
 	   int j;
 	   int i;
 	 int carp;
       int[] list;
        int plats;
 	int b;
 	int a;
 		z	= 15;
 		y = 3;
 		jamfor = m.get(n,k);
 	   j = k;
        while(0 < j){
 			i = n;
 	  while(0 < i-(k-j)){
 	     jamfor = m.max(m.get(i,j),jamfor);
 	     carp = m.set(i-1,j-1,jamfor + m.get(i-1,j-1));
 	    i = i-1;
 			}
 			j = j-1;
 	}
 
 
 		list = new int[k+1];
 			plats = 0;
 			b = 0;
 	while(b < k+1){
 	    jamfor = m.get(plats+1,b);
 			a = plats+1;
 	    while(a < n-(k-b-1)){
 		jamfor = m.max(m.get(a,b),jamfor);
 		if (!(jamfor < m.get(a,b)) && !(m.get(a,b)< jamfor)){
 		    plats = a;
 		}else{}
 				a = a+1;
 	    }
 	  list[b] = plats+1;
 			b = b+1;
 	}
 	return list;
     }
 }
 
 class Matrix{
 	int[] a1;
 	int[] a2;
 	int[] a3;
 	public int get(int y, int x){
 		int i;
 		i = 0;
 		if(!(x < 0) && !(0 < x)){
 			i = a1[y];
 		}else{}
 		if(!(x < 1) && !(1 < x)){
 			i = a2[y];
 		}else{}
 		if(!(x < 2) && !(2 < x)){
 			i = a3[y];
 		}else{}
 		return i;
 	}
 
 
 	public int set(int y, int x, int value){
 		if(!(x < 0) && !(0 < x)){
 			a1[y] = value;
 		}else{}
 		if(!(x < 1) && !(1 < x)){
 			a2[y] = value;
 		}else{}
 		if(!(x < 2) && !(2 < x)){
 			a3[y] = value;
 		}else{}
 		return 0;
 	}
 
 	public int start(){
 		int i;
 		a1 = new int[15];
 		a2 = new int[15];
 		a3 = new int[15];
 		i = 0;
 		a1[0] = 7;
 		a1[1] = 2;
 		a1[2] = 3;
 		a1[3] = 1;
 		a1[4] = 8;
 		a1[5] = 9;
 		a1[6] = 3;
 		a1[7] = 3;
 		a1[8] = 2;
 		a1[9] = 7;
 		a1[10] = 2;
 		a1[11] = 9;
 		a1[12] = 1;
 		a1[13] = 0;
 		a1[14] = 8;
 		i = 0;
 		a2[0] = 0;
 		a2[1] = 14;
 		a2[2] = 13;
 		a2[3] = 2;
 		a2[4] = 3;
 		a2[5] = 2;
 		a2[6] = 2;
 		a2[7] = 1;
 		a2[8] = 3;
 		a2[9] = 5;
 		a2[10] = 4;
 		a2[11] = 1;
 		a2[12] = 0;
 		a2[13] = 2;
 		a2[14] = 0;
 		i = 0;
 		a3[0] = 3;
 		a3[1] = 4;
 		a3[2] = 3;
 		a3[3] = 7;
 		a3[4] = 8;
 		a3[5] = 4;
 		a3[6] = 1;
 		a3[7] = 8;
 		a3[8] = 5;
 		a3[9] = 6;
 		a3[10] = 4;
 		a3[11] = 1;
 		a3[12] = 0;
 		a3[13] = 2;
 		a3[14] = 1;
 		return 0;
 	}
 
 	public int max(int a, int b){
 		int i;
 		if(a < b)
 			i = b;
 		else
 			i = a;
 		return i;
 	}
 
 }
