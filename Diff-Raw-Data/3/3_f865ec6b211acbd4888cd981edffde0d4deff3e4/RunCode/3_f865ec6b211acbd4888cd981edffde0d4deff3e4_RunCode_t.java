 package mindpin.java_step_tester.compilation;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import mindpin.java_step_tester.junit4.Listener;
 import mindpin.java_step_tester.modle.Assets;
 import mindpin.java_step_tester.modle.ResponseModle;
 import mindpin.java_step_tester.utils.FileUtil;
 
 import org.junit.runner.JUnitCore;
 import org.junit.runner.notification.Failure;
 
 import com.google.gson.Gson;
 
 public class RunCode {
 	public static String FULL_CLASS_NAME = "InputTest";
 	
 	private HashMap<String, Boolean> test_map = new HashMap<String, Boolean>();
 	private HashMap<String, String> test_map_doc  = new HashMap<String, String>();
 	private HashMap<String, Failure> test_map_error = new HashMap<String, Failure>();
 	private String classPath;
 	private String input;
 	private String rule;
 	
 	
 	public RunCode(String input, String rule){
 		long threadId = Thread.currentThread().getId();
 		System.out.println("--------------------------- "+threadId+" ---------------------------");
		this.classPath = System.getProperty("java.io.tmpdir") + File.separator + "dyncompiler" + threadId;
//		this.classPath = System.getProperty("user.dir") + File.separator + "dyncompiler" + threadId;
 		this.input = input;
 		this.rule = rule;
 	}
 	
 	public String get_full_source_code(){
 		// 1.创建需要动态编译的代码字符串
 		String nr = "\r\n"; //回车
 		return 
 				
 				" import mindpin.java_step_tester.junit4.TestDescription;" + nr +
 				
 				" import org.junit.Assert; " + nr +
 				" import org.junit.Test; " + nr +
 				" import org.junit.runner.RunWith; " + nr +
 				" import org.junit.runners.JUnit4; " + nr +
 				
 				" import java.util.Iterator;" + nr +
 				" import java.util.Set;" + nr +
 				" import org.junit.runner.JUnitCore;" + nr +
 				" import org.junit.runner.Result;" + nr +
 				
 				" @RunWith(JUnit4.class) " + nr +
 				" public class  InputTest{" + nr + 
 					 this.rule + nr +
 				" }" + nr +
 				
 				" class RuleTest{" + nr + 
 					this.input + nr +
 				" }";
 	}
 	
 	public String return_json(ResponseModle responseModle){
 		Gson gson = new Gson();
 		String responseModle_json = gson.toJson(responseModle);
 		
 		System.out.println(responseModle_json);
 		return responseModle_json;
 	}
 	
 	public String run(){
 		JUnitCore core = new org.junit.runner.JUnitCore();
 		Listener listener = new Listener(test_map, test_map_doc, test_map_error);
 		core.addListener(listener);
 		int i = 0;
 		List<Assets> assets_list = new ArrayList<Assets>();
 
 		try {
 			Class<?> clz = new MyClassLoader(this.classPath).loadClass(FULL_CLASS_NAME);
 			new MyClassLoader(this.classPath).loadClass("RuleTest");
 			Object myObj = clz.newInstance();
 			core.run(myObj.getClass());
 		}catch (Exception e) {
 			i++;
 //			e.printStackTrace();
 			System.out.println("=================编译错误=====================");
 			return return_json(new ResponseModle("代码编译异常", false, assets_list));
 		}
 		
 		
 		Set<String> set = test_map.keySet();
 		Iterator<String> it = set.iterator();
 		
 		while(it.hasNext()){
 			String name = it.next();
 
 			String doc = test_map_doc.get(name) == null ?  "":test_map_doc.get(name);
 			
 			boolean is_success = test_map.get(name);
 			Failure failure = test_map_error.get(name);
 			String exception = "";
 			if (!is_success) {
 				exception = failure.getException().getClass() == AssertionError.class ? "":failure.getException().toString();
 			}
 			
 			i += is_success ?  0:1;
 			
 			Assets assets = new Assets(doc, is_success, exception);
 			assets_list.add(assets);
 		}
 
 		boolean success = i==0;
 		ResponseModle responseModle = new ResponseModle("", success, assets_list);
 		return return_json(responseModle);
 	}
 	
 	public String get_result(){
 		
 		String full_source_code = get_full_source_code();
 		try {
 			new MyClassCompiler(classPath,FULL_CLASS_NAME, full_source_code).compile();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String result = run();
 		FileUtil.delFolder(classPath);
 			
 		
 		return "" + result;
 	}
 	
 }
