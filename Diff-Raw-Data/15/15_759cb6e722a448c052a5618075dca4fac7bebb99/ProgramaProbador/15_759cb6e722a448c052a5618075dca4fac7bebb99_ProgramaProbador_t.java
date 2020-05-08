 package programaProbador;
 
 import java.util.Random;
 
 
 import sel.Sel;
 import vectorMath.VectorMath;
 import matrizMath.MatrizMath;
 
 
 
 public class ProgramaProbador {
 
 	/**
 	 * @param args
 	 */	
 	
 
 	
 	public void GeneraInput(Sel sistEcLineal)
 	{
 
 	
 	}
 	public static void PruebaInversa()
 	{
 		int dim =50;
 		MatrizMath mPrueba = new MatrizMath(dim, dim);
 		MatrizMath mInversa = null;
 		MatrizMath mIdentidadReal = null;
 		boolean correcto = true;
 		Random r1 = new Random();
 		
 		int count =0;
 		double determinante = 0;
 		
 		while (correcto && count < 1000)
 		{
 			mPrueba.generaMatrizAleatoria();
 			System.out.printf ("Matriz generada numero %d, dimension: %d %d\n",count+1,dim,dim);
 			System.out.println (mPrueba);
 			
 			mIdentidadReal = mPrueba.identidad();
 			
 			determinante = mPrueba.determinante2();
 			System.out.printf("Determinante %f: \n", determinante);
 			
 			System.out.printf ("Matriz inversa numero %d\n",count+1,dim,dim);
 			mInversa = mPrueba.inversa();
 			System.out.println (mInversa);
 			
 			
 			if (!MatrizMath.comparaDeterminantes(determinante, 0))
 			{
 			System.out.printf ("Producto Matriz x Inversa numero %d\n",count+1,dim,dim);
 			System.out.println (mPrueba.producto(mInversa));
 			}
 			
 			System.out.printf ("Matriz Identidad numero %d\n",count+1,dim,dim);
 			System.out.println (mPrueba.identidad());
 			
 			
 			if (!mPrueba.producto(mInversa).equals(mIdentidadReal))
 			{
 				System.out.printf("Incorrecto");
 				correcto = false;
 			}
 			count++;
 		
 		}
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		MatrizMath mPrueba = null;
 		MatrizMath mInversa = null;
 		boolean correcto = true;
 		Random r1 = new Random();
 		int dim =0;
 		int count = 0;
 		Double determinante = 0.00;
 		
 		
 		VectorMath vectorPrueba;
 		
 
 		
 		
 
			dim =100; //r1.nextInt(4);
 			if (dim >1)
 			{
 			mPrueba= new MatrizMath(dim,dim);
 			mPrueba.generaMatrizAleatoria();
 			
 			vectorPrueba = new VectorMath(dim);
 			vectorPrueba.generaVectorAleatorio();
 			
 			Sel selPrueba = new Sel(mPrueba, vectorPrueba);
 			selPrueba.generaInput();
 			
 			selPrueba.resolver();
 			
 			System.out.println(selPrueba);
 			}
 			
 		
 
 		}
 	
 
 }
