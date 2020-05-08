 package backend.computations.operations;
 
 import backend.blocks.*;
 import static org.junit.Assert.*;
 import org.junit.*;
 
 import backend.computations.infrastructure.*;
 
 public class M_ColumnspaceTest
 {
     @Test
 	public void size1() throws Exception
 	{
 		Double[][] v=new Double[1][1];
 		v[0][0]=new Double(5);
		Matrix m=new Matrix(null,v);
 		M_Columnspace r=new M_Columnspace(m);
 		Solution s=r.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==1);
 		assertTrue(a[0].length==1);
 		assertTrue(a[0][0]==5);
 	}
 
 	@Test
 	public void size2x2() throws Exception
 	{
 		Double[][] v=new Double[2][2];
 		v[0][0]=new Double(2);
 		v[1][1]=new Double(2);
 		v[0][1]=new Double(0);
 		v[1][0]=new Double(1);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==2);
 		assertTrue(a[0].length==2);
 		assertTrue(a[0][0]==2);
 		assertTrue(a[1][0]==1);
 		assertTrue(a[0][1]==0);
 		assertTrue(a[1][1]==2);
 	}
 
 	@Test
 	public void nullvalue()
 	{
 		Double[][] v=new Double[2][2];
 		v[0][0]=new Double(5);
 		v[0][1]=new Double(10);
 		v[1][0]=null;
 		v[1][1]=new Double(2);
		Matrix m=new Matrix(null,v);
 		try
 		{
 			new M_Columnspace(m);
 			assertTrue(false);
 		}
 		catch (Exception e)
 		{
 			assertTrue(true);
 		}
 	}
 
 	@Test
 	public void size2x1() throws Exception
 	{
 		Double[][] v=new Double[2][1];
 		v[0][0]=new Double(2);
 		v[1][0]=new Double(1);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==1);
 		assertTrue(a[0].length==1);
 		assertTrue(a[0][0]==2);
 	}
 
 	@Test
 	public void size1x2() throws Exception
 	{
 		Double[][] v=new Double[1][2];
 		v[0][0]=new Double(33);
 		v[0][1]=new Double(6);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==1);
 		assertTrue(a[0].length==2);
 		assertTrue(a[0][0]==33);
 		assertTrue(a[0][1]==6);
 	}
 
 	@Test
 	public void ref3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(3);
 		v[2][1]=new Double(4);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(-9);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==3);
 		assertTrue(a[0].length==3);
 		for (int i=0;i<3;i++)
 		{
 			for (int j=0;j<3;j++)
 			{
 				assertTrue(a[i][j].equals(v[i][j]));
 			}
 		}
 	}
 
 	@Test
 	public void lowerrank3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(0);
 		v[2][1]=new Double(4);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(-9);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==2);
 		assertTrue(a[0].length==3);
 		for (int i=0;i<3;i++)
 		{
 			assertTrue(a[0][i].equals(v[0][i]));
 			assertTrue(a[1][i].equals(v[2][i]));
 		}
 	}
 
 	@Test
 	public void lowerrankmid3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(1);
 		v[2][1]=new Double(4);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(1);
 		v[2][2]=new Double(4);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==2);
 		assertTrue(a[0].length==3);
 		for (int i=0;i<3;i++)
 		{
 			assertTrue(a[0][i].equals(v[0][i]));
 			assertTrue(a[1][i].equals(v[1][i]));
 		}
 	}
 
 	@Test
 	public void lowestrank3x3() throws Exception
 	{
 		Double[][] v=new Double[3][3];
 		v[0][0]=new Double(1);
 		v[1][0]=new Double(2);
 		v[2][0]=new Double(3);
 		v[0][1]=new Double(0);
 		v[1][1]=new Double(0);
 		v[2][1]=new Double(0);
 		v[0][2]=new Double(0);
 		v[1][2]=new Double(0);
 		v[2][2]=new Double(0);
		Matrix m=new Matrix(null,v);
 		M_Columnspace d=new M_Columnspace(m);
 		Solution s=d.getSolution();
 		Double[][] a=((Matrix)(s.getAnswer())).getValues();
 		assertTrue(a.length==1);
 		assertTrue(a[0].length==3);
 		for (int i=0;i<3;i++)
 		{
 			assertTrue(a[0][i].equals(v[0][i]));
 		}
 	}
 }
