 package tests.generics;
 
 import java.util.ArrayList;
 
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.GenericTypeVar;
 import ecologylab.serialization.annotations.simpl_composite;
 
public class MediaSearchResult<M extends Media> extends SearchResult
 {
 	// M media;
 	
 	@simpl_composite
 	public MediaSearch<M, MediaSearchResult<? extends Media>>	ms;
 
 	@simpl_composite
 	public MediaSearchResult<Media>									msr;
 
 	public MediaSearchResult()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	public static void main(String args[])
 	{
 		ClassDescriptor c = ClassDescriptor.getClassDescriptor(MediaSearchResult.class);
 		
 		System.out.println("generic params of class " + c.getDescribedClassName());
 		print(c.getGenericTypeVars());
 		
 		for(Object o : c.getAllFieldDescriptorsByTagNames().values())
 		{
 			FieldDescriptor f = (FieldDescriptor) o;
 			
 			System.out.println("generic params of field " + f.getField().getName());
			print(f.getGenricTypeVars());
 		}
 	}
	
 	public static void print(ArrayList<GenericTypeVar> vars)
 	{
 		System.out.print("[");
 		int i = 0;
 		for (GenericTypeVar g : vars)
 		{
 			if (i++ > 0)
 				System.out.print(", ");
 			System.out.print(g);
 		}
 		System.out.println("]");
 	}
 }
