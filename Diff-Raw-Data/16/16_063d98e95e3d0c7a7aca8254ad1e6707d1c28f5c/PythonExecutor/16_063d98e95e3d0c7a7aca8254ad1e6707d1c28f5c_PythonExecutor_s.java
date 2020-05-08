 package org.ourgrid.browser.client.executor;
 
 
 public class PythonExecutor {
 
 	public native void initialize() /*-{
   		$wnd.Python.initialize(null, function(chr) {
         	if (chr !== null) {
        		$wnd.output.value += String.fromCharCode(chr);
        		$wnd.output.scrollTop = $wnd.output.scrollHeight;
         	}
         });
   	}-*/;
 	
 	public native void reset() /*-{
 		$wnd._Py_Finalize();
 		$wnd._Py_Initialize();
 		
 		var cmd = $wnd.Python.allocateString('python');
     	$wnd._PySys_SetArgv(1, $wnd.allocate([cmd], 'i8*', $wnd.ALLOC_NORMAL));
 		
 		$wnd.Python.module = $wnd._PyImport_AddModule(
 			$wnd.Python.allocateString('__main__'));
     	$wnd.Python.filename = $wnd.Python.allocateString('<stdin>');
     	$wnd.Python.flags = $wnd.allocate([0], 'i32', $wnd.ALLOC_NORMAL);
     	$wnd.Python.globals = $wnd._PyModule_GetDict($wnd.Python.module);
 		
 	}-*/;
 
 	public native void eval(String content) /*-{
 		$wnd.Python.eval(content);
 	}-*/;
 	
 }
