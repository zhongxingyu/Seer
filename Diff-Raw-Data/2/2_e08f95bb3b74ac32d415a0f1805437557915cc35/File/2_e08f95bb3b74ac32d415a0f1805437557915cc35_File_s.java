 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import db.Resources;
 
 public class File
 {
 
 	private String			fileName;
 	
 	private int				startChar;
 	private int				endChar;
 
 	private List<String>	fileImports;
 	private String			filePackage;
 	private List<Clazz>		fileClazzes;
 	private List<Clazz>		fileInterfaces;
 	
 	public File()
 	{
 		fileImports = new ArrayList<String>();
 		fileClazzes = new ArrayList<Clazz>();
 		fileInterfaces = new ArrayList<Clazz>();
 		filePackage = "";
 	}
 
 	public File(List<String> fileImports, String filePackage,
 			List<Clazz> fileClazzes, List<Clazz> fileInterfaces)
 	{
 		super();
 		this.fileImports = fileImports;
 		this.filePackage = filePackage;
 		this.fileClazzes = fileClazzes;
 		this.fileInterfaces = fileInterfaces;
 	}
 
 	public void print()
 	{
 		if (!this.filePackage.equals("java.util"))
 		{
 			System.out.println("FILE: " + fileName);
 			System.out.println("  Package: " + filePackage);
 			System.out.println("  Imports: ");
 			for (String imp : fileImports)
 				System.out.println("    " + imp);
 			for (Clazz clazz : fileClazzes)
 				clazz.print();
 		}
 	}
 
 	public void addFileImport(String fileImport)
 	{
 		this.fileImports.add(fileImport);
 	}
 
 	public void addClazz(Clazz clazz)
 	{
 		this.fileClazzes.add(clazz);
 	}
 
 	public void addInterface(Clazz clazz)
 	{
 		this.fileInterfaces.add(clazz);
 	}
 
 	public Clazz hasUnresolvedClazz(String c)
 	{
 		for (Clazz clazz : fileClazzes)
 		{
 			String unresolved = clazz.getName();
 			unresolved = unresolved.substring(unresolved.lastIndexOf(".") + 1,
 					unresolved.length());
 
 			if (c.equals(unresolved))
 			{
 				System.out.println("Found " + c + " in class "
 						+ this.getFileName());
 				return clazz;
 			}
 		}
 
 		return null;
 	}
 
 	public Clazz hasUnresolvedInterface(String c)
 	{
 		for (Clazz clazz : fileInterfaces)
 		{
 			String unresolved = clazz.getName();
 			unresolved = unresolved.substring(unresolved.lastIndexOf(".") + 1,
 					unresolved.length());
 
 			if (c.equals(unresolved))
 			{
 				System.out.println("Found " + c + " in class "
 						+ this.getFileName());
 				return clazz;
 			}
 		}
 
 		return null;
 	}
 	
 	private void updateWeight(Set<WeightedChange> weights, Change change, float weight) {
 		for(WeightedChange wc: weights) {
 			if(wc.getOwnerId().equals(change.getOwnerId())) {
 				wc.setWeight(wc.getWeight()+weight);
 				return;
 			}
 		}
 		
 		WeightedChange newWeight = new WeightedChange(change, weight);
 		weights.add(newWeight);
 	}
 	
 	public Set<WeightedChange> getMethodWeights(List<Change> changes, Method method )
 	{
 		Set<WeightedChange> weights = new HashSet<WeightedChange>();
 		for (Change change : changes)
 		{
 			float weight = getMethodWeight(change, method);
 			if (weight > 0)
 				updateWeight(weights, change, weight);
 		}
 		return weights;
 	}
 	
 	public float getMethodWeight(Change change, Method method) {
 
 		float sum = 0;
 		// Case 1
 		if(change.getCharStart() < method.getstartChar() 
 				&& (change.getCharEnd() >= method.getstartChar() && change.getCharEnd() < method.getendChar())) {
 			sum = change.getCharEnd() - method.getstartChar();
 		}
 		// Case 2
 		else if((change.getCharStart() >= method.getstartChar() && change.getCharStart() < method.getendChar()) 
 				&& change.getCharEnd() > method.getstartChar() && change.getCharEnd() <= method.getendChar()) {
 			sum= change.getCharEnd() - change.getCharStart();
 		}
 		// Case 3
 		else if((change.getCharStart() > method.getstartChar() && change.getCharStart() <= method.getendChar())
 				&& change.getCharEnd() > method.getendChar()) {
 			sum = method.getendChar() - change.getCharStart();
 		}
 		// Case 4
 		else if(change.getCharStart() < method.getstartChar() && change.getCharEnd() > method.getendChar()) {
 			sum = method.getendChar() - method.getstartChar();
 		}
 
 		// Get the percentage
 		float weight = (sum / (method.getendChar() - method.getstartChar()));
 
 		return weight;
 	}
 
 	public String getFileName()
 	{
 		return fileName;
 	}
 
 	public void setFileName(String fileName)
 	{
 		this.fileName = fileName;
 	}
 
 	public List<String> getFileImports()
 	{
 		return fileImports;
 	}
 
 	public void setFileImports(List<String> fileImports)
 	{
 		this.fileImports = fileImports;
 	}
 
 	public String getFilePackage()
 	{
 		return filePackage;
 	}
 
 	public void setFilePackage(String filePackage)
 	{
 		this.filePackage = filePackage;
 	}
 
 	public List<Clazz> getFileClazzes()
 	{
 		return fileClazzes;
 	}
 
 	public void setFileClazzes(List<Clazz> fileClazzes)
 	{
 		this.fileClazzes = fileClazzes;
 	}
 
 	public List<Clazz> getFileInterfaces()
 	{
 		return fileInterfaces;
 	}
 
 	public void setFileInterfaces(List<Clazz> fileInterfaces)
 	{
 		this.fileInterfaces = fileInterfaces;
 	}
 
 	public int getStartChar()
 	{
 		return startChar;
 	}
 
 	public void setStartChar(int startChar)
 	{
 		this.startChar = startChar;
 	}
 
 	public int getEndChar()
 	{
 		return endChar;
 	}
 
 	public void setEndChar(int endChar)
 	{
 		this.endChar = endChar;
 	}
 }
