 /**
  * @(#)Utils.java
  */
 
 package aurora.hwc.report;
 
 import java.io.File;
 import java.util.*;
 import javax.swing.tree.*;
 
 
 /**
  * Miscellaneous utilities for the report generator
  * @author Gabriel Gomes
  */
 public class Utils {
 	
 	public static boolean verbose = false;
 	public static String outfilename = null;
 	public static enum ExporterType {pdf,ppt,xls};
 	
 	public static HashMap<ExporterType,String> exportType2String = new HashMap<ExporterType,String>();  
 	public static HashMap<String,ExporterType> exportString2Type = new HashMap<String,ExporterType>();  
 	
 	public static HashMap<ReportType,String> reportType2String = new HashMap<ReportType,String>();   
 	public static HashMap<String,ReportType> reportString2Type = new HashMap<String,ReportType>();  
     
 	static{
 		
 		// populate report type hashmaps
		reportType2String.put(ReportType.vehicletypes,"Compare vehicle types, single run");
		reportType2String.put(ReportType.aggvehicletypes,"Aggregate vehicle types, multiple runs");
		reportType2String.put(ReportType.bestworst,"Best/Worst case performance");
		reportType2String.put(ReportType.scatter, "Scatter plot");
 		Set<ReportType> c1 = Utils.reportType2String.keySet();
 	    Iterator<ReportType> it1 = c1.iterator();
 	    while(it1.hasNext()){
 	    	ReportType rtype = (ReportType) it1.next();
 	    	reportString2Type.put(reportType2String.get(rtype), rtype);
 	    }
 	    
 	    // populate export type hashmaps
 	    exportType2String.put(ExporterType.pdf,"PDF");
 	    exportType2String.put(ExporterType.ppt,"MS Powerpoint");
 	    exportType2String.put(ExporterType.xls,"MS Excel");
 		Set<ExporterType> c2 = Utils.exportType2String.keySet();
 	    Iterator<ExporterType> it2 = c2.iterator();
 	    while(it2.hasNext()){
 	    	ExporterType etype = (ExporterType) it2.next();
 	    	exportString2Type.put(exportType2String.get(etype),etype);
 	    }
 	    
 	}
 	
 	public static void writeToConsole(final String str){
 		if(Utils.verbose)
 			System.out.println(str);
 		//private static int maxconsolelines = 400;
 		//private static int consolelinecount=0;
 		/*
 		consolelinecount++;
 		if(consolelinecount>maxconsolelines){
 			try {
 				txt_console.replaceRange("",txt_console.getLineStartOffset(0),txt_console.getLineStartOffset(1));
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 		}
 		txt_console.append(str + "\n");
 		*/
 	}
 	
 	public static boolean isInteger(String s) {
 		try {
 			Integer.parseInt(s);
 		}
 		catch (NumberFormatException nfe) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean isNumber(String s) {
 		try {
 			Double.parseDouble(s);
 		}
 		catch (NumberFormatException nfe) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static void splitbydelimiter(String str,String delimiter,Vector<String> out){
 		out.clear();
 		StringTokenizer strtok = new StringTokenizer(str,delimiter);
 		while(strtok.hasMoreTokens()){
 			out.add(strtok.nextToken());
 		}
 	}
 	
 	public static void readMatlabFormattedFloatMatrix(String str,Vector< Vector<Float> > M){
 		M.clear();
 		StringTokenizer st1 = new StringTokenizer(str, ";");
 		while (st1.hasMoreTokens()) {
 			String bufR = st1.nextToken();
 			StringTokenizer st2 = new StringTokenizer(bufR, ",");
 			Vector<Float> C = new Vector<Float>();
 			while (st2.hasMoreTokens())
 				C.add( Float.parseFloat(st2.nextToken()) );
 			M.add(C);
 		}
 	}
 
 	public static void readMatlabFormattedFloatVector(String str, Vector<Float> V){
 		V.clear();
 		StringTokenizer st1 = new StringTokenizer(str, ",");
 		while (st1.hasMoreTokens()){
 			V.add( Float.parseFloat(st1.nextToken()) );
 		}
 	}
 
 	public static void readMatlabFormattedStringVector(String str,Vector<String> V){
 		V.clear();
 		StringTokenizer st1 = new StringTokenizer(str, ",");
 		while (st1.hasMoreTokens()){
 			V.add( st1.nextToken() );
 		}
 	}
 	
 	public static <T> String writeMatlabFormattedVector(Vector<T> V){
 		String str = new String("");
 		if(V.isEmpty())
 			return str;
 		str = str + V.get(0).toString();
 		for(int i=1;i<V.size();i++)
 			str = str + "," + V.get(i).toString();
 		return str;
 	}
 
 	public static <T> String writeMatlabFormattedMatrix(Vector<Vector<T>> M){
 		String str = new String("");
 		if(M.isEmpty())
 			return str;
 		str = str + writeMatlabFormattedVector(M.get(0));
 		for(int i=1;i<M.size();i++)
 			str = str + ";" + writeMatlabFormattedVector(M.get(i));
 		return str;
 	}
 	
 	public static TreePath getPath(TreeNode treeNode) {
 		List<Object> nodes = new ArrayList<Object>();
 		if (treeNode != null) {
 			nodes.add(treeNode);
 			treeNode = treeNode.getParent();
 			while (treeNode != null) {
 				nodes.add(0, treeNode);
 				treeNode = treeNode.getParent();
 			}
 		}
 		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
 	}
 	
 	public static void hex2rgb(Vector<String> hex,Vector<Vector<Float>> rgb){
 		char buf[] = new char[2]; 
 		int temp=0;
 		rgb.clear();
 		for(int i=0;i<hex.size();i++){
 			Vector<Float> q = new Vector<Float>();			
 			if(hex.get(i).length()!=7){
 				q.add(Float.NaN);
 				q.add(Float.NaN);
 				q.add(Float.NaN);
 			}
 			else{
 				hex.get(i).getChars(1,3,buf,temp);
 				q.add( ((float) Integer.parseInt(new String(buf),16))/255 );
 				hex.get(i).getChars(3,5,buf,temp);
 				q.add( ((float) Integer.parseInt(new String(buf),16))/255 );
 				hex.get(i).getChars(5,7,buf,temp);
 				q.add( ((float) Integer.parseInt(new String(buf),16))/255 );
 			}
 			rgb.add(q);
 		}
 	}
 	
 	public static void rgb2hex(Vector<Vector<Float>> rgb, Vector<String> hex){
 		int i,j;
 		String strA = new String("");
 		String strB = new String("");
 		
 		hex.clear();
 		for(i=0;i<rgb.size();i++){
 			strA = "#";
 			for(j=0;j<3;j++){
 				Float f = (Float) rgb.get(i).get(j)*255f;	
 				strB = Integer.toHexString(f.intValue()).toUpperCase();
 				if(strB.length()==1)
 					strB = "0" + strB;
 				strA = strA + strB;
 			}
 			hex.add( strA );
 		}			
 	}
 	
 	public static <T> Vector<T> vectorintersect(Vector<T> A,Vector<T> B){
 		Vector<T> C = new Vector<T>();
 		for(int i=0;i<A.size();i++)
 			if(B.contains(A.get(i)))
 				C.add(A.get(i));
 		return C;
 	}
 	
 	public static Vector<Vector<Float>> multiplyoverspace(Vector<Vector<Float>> A,Vector<Float> L){
 		if(A.size()!=L.size())
 			return null;
 		Vector<Vector<Float>> AL = new Vector<Vector<Float>>();
 		int i,j;
 		for(i=0;i<A.size();i++){
 			AL.add( new Vector<Float>() );
 			for(j=0;j<A.get(i).size();j++){
 				AL.get(i).add( A.get(i).get(j)*L.get(i) );
 			}
 		}
 		return AL;
 	}
 
 	public static Vector<Vector<Float>> divide(Vector<Vector<Float>> A,Vector<Vector<Float>> B){
 		Vector<Vector<Float>> AoverB = new Vector<Vector<Float>>();
 		int i,j;
 		for(i=0;i<A.size();i++){
 			AoverB.add(new Vector<Float>());
 			for(j=0;j<A.get(i).size();j++){
 				AoverB.get(i).add( A.get(i).get(j) / B.get(i).get(j) );
 			}
 		}
 		return AoverB;
 	}
 
 	public static Vector<Vector<Float>> subtract(Vector<Vector<Float>> A,Vector<Vector<Float>> B){
 		Vector<Vector<Float>> Aminus = new Vector<Vector<Float>>();
 		int i,j;
 		for(i=0;i<A.size();i++){
 			Aminus.add(new Vector<Float>());
 			for(j=0;j<A.get(i).size();j++){
 				Aminus.get(i).add( A.get(i).get(j) - B.get(i).get(j) );
 			}
 		}
 		return Aminus;
 	}
 	
 	public static Vector<Vector<Float>> oneMinus(Vector<Vector<Float>> A){
 		Vector<Vector<Float>> OneMinusA = new Vector<Vector<Float>>();
 		int i,j;
 		for(i=0;i<A.size();i++){
 			OneMinusA.add(new Vector<Float>());
 			for(j=0;j<A.get(i).size();j++){
 				OneMinusA.get(i).add( 1f-A.get(i).get(j) );
 			}
 		}
 		return OneMinusA;
 	}
 
 	public static void boundbelow(Vector<Vector<Float>> A,float x){
 		int i,j;
 		for(i=0;i<A.size();i++){
 			for(j=0;j<A.get(i).size();j++){
 				if(A.get(i).get(j)<x)
 					A.get(i).set(j,x);
 			}
 		}
 	}
 
 	public static void boundabove(Vector<Vector<Float>> A,float x){
 		int i,j;
 		for(i=0;i<A.size();i++){
 			for(j=0;j<A.get(i).size();j++){
 				if(A.get(i).get(j)>x)
 					A.get(i).set(j,x);
 			}
 		}
 	}
 
 	public static Vector<Vector<Float>> isgreater(Vector<Vector<Float>> A,Vector<Vector<Float>> B){
 		Vector<Vector<Float>> AisgreaterB = new Vector<Vector<Float>>();
 		int i,j;
 		for(i=0;i<A.size();i++){
 			AisgreaterB.add(new Vector<Float>());
 			for(j=0;j<A.get(i).size();j++){
 				if(A.get(i).get(j)>B.get(i).get(j))
 					AisgreaterB.get(i).add(1f);
 				else
 					AisgreaterB.get(i).add(0f);
 			}
 		}
 		return AisgreaterB;
 	}
 		
 	public static void keepSubsetFirstDimension(Vector<Vector<Float>> M,Vector<Integer> subsetrows){
 		Iterator<Vector<Float>> itr = M.iterator();
 		int rowcount = 0;
 		while(itr.hasNext()){
 			itr.next();
 			if(!subsetrows.contains(rowcount))
 				itr.remove();
 			rowcount++;
 		}
 	}
 	
 	public static void keepSubset(Vector<Float> V,Vector<Integer> subsetrows){
 		Iterator<Float> itr = V.iterator();
 		int rowcount = 0;
 		while(itr.hasNext()){
 			itr.next();
 			if(!subsetrows.contains(rowcount))
 				itr.remove();
 			rowcount++;
 		}
 	}
 	
 	public static Vector<Float> cumsum(Vector<Float> a){
 		Vector<Float> z = new Vector<Float>();
 		z.add(0f);
 		for(int i=0;i<a.size();i++)
 			z.add( z.lastElement() + a.get(i) );
 		z.remove(0);
 		return z;
 	}
 	
 	public static Vector<Vector<Float>> sumoverdimension3(Vector<Vector<Vector<Float>>> phi,Quantity dim){
 		Vector<Vector<Float>> z = new Vector<Vector<Float>>();
 		Vector<Vector<Float>> P;
 		float f;
 		int i,ii,jj;
 		int numtime,numspace;
 		
 		for(i=0;i<phi.size();i++){
 			Vector<Float> z1 = new Vector<Float>();
 			P = phi.get(i);
 			numspace = P.size();
 			numtime = P.get(0).size();
 			
 			switch(dim){
 			case time:		// time aggregation case
 				for(ii=0;ii<numspace;ii++){
 					f = 0;
 					for(jj=0;jj<numtime;jj++){
 						f += P.get(ii).get(jj);
 					}
 					z1.add(f);
 				}
 				break;
 
 			case space:		// space aggregation case
 				for(jj=0;jj<numtime;jj++){
 					f = 0;
 					for(ii=0;ii<numspace;ii++){
 						f += P.get(ii).get(jj);
 					}
 					z1.add(f);
 				}
 				break;
 			}
 			
 			z.add(z1);
 		}
 		return z;
 	}
 	
 	public static Vector<Float> sumoverdimension2(Vector<Vector<Float>> phi,Quantity dim){
 				
 		Vector<Float> z = new Vector<Float>();
 		float f;
 		int ii,jj;
 		int numspace = phi.size();
 		int numtime = phi.get(0).size();
 		
 		switch(dim){
 		case time:		// time aggregation case
 			for(ii=0;ii<numspace;ii++){
 				f = 0;
 				for(jj=0;jj<numtime;jj++){
 					f += phi.get(ii).get(jj);
 				}
 				z.add(f);
 			}
 			break;
 
 		case space:		// space aggregation case
 			for(jj=0;jj<numtime;jj++){
 				f = 0;
 				for(ii=0;ii<numspace;ii++){
 					f += phi.get(ii).get(jj);
 				}
 				z.add(f);
 			}
 			break;
 		}
 
 		return z;
 	}
 	
 	public static void multiplyscalar2(Vector<Vector<Float>> A,float x){
 		for(int i=0;i<A.size();i++)
 			for(int j=0;j<A.get(i).size();j++)
 				A.get(i).set(j, A.get(i).get(j)*x );
 	}
 		
 	public static void multiplyscalar1(Vector<Float> A,float x){
 		for(int i=0;i<A.size();i++)
 			A.set(i, A.get(i)*x );
 	}
 	
 	public static float sum(Vector<Float> A){
 		float z = 0;
 		for(int i=0;i<A.size();i++)
 			z += A.get(i);
 		return z;
 	}
 	
 	/**
 	 * Return file extension.
 	 */
 	public static String getExtension(File f) {
 		String ext = null;
 		String s = f.getName();
 		int i = s.lastIndexOf('.');
 		if ((i > 0) && (i < (s.length() - 1)))
 			ext = s.substring(i+1).toLowerCase();
 		if (ext == null)
 			return "";
 		return ext;
 	}
 }
