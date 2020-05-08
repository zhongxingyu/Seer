 package main;
 
 import java.io.PrintStream;
 
 import syntaxtree.Absyn;
 import errors.ErrorEchoer;
 
 class SimpleError implements ErrorEchoer
 {
     private String sourceName;
     
     private int ec;
     private int wc;
     private PrintStream err;
	private boolean has_error;
 
     public SimpleError(PrintStream e, String s)
     {
         super();
         
         ec = wc = 0;
         
         err = e;
         
         sourceName = s;
     }
     
     public SimpleError()
     {
         this(System.err, "-- UNKNOWN SOURCE --");
		has_error = false;
     }
     
     public SimpleError(String s)
     {
         this(System.err, s);
     }
     
     public SimpleError(PrintStream s)
     {
         this(s, "-- UNKNOWN SOURCE --");
     }
 
     public void Print(Object[] msg)
     {
		has_error = true;
         err.print(sourceName + ":");
         for ( Object o : msg )
             err.println(" " + o);
     }
 
     public void Error(Absyn obj, Object[] msg)
     {
         ec++;
         
         err.println("Erro em " + sourceName + "[" + obj.line + "," + obj.row + "]:");
         for ( Object o : msg )
             err.println("    " + o);
     }
 
     public void Warning(Absyn obj, Object[] msg)
     {
         wc++;
         
         err.println("Aviso em " + sourceName + "[" + obj.line + "," + obj.row + "]:");
         for ( Object o : msg )
             err.println("    " + o);
     }
 
     public int ErrorCount()
     {
         return ec;
     }
 
     public int WarningCount()
     {
         return wc;
     }
 
     public void Reset()
     {
         ec = wc = 0;
     }
 
	public boolean hasError()
	{
		return has_error;
	}

 }
