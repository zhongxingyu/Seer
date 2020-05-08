 package com.nerds;
 
 import java.io.*;
 import java.net.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class AntScriptDefaultRuntime extends AntScriptRuntime {
 	public void setUp(AntScriptCore ar) {
 		ar.addFunction("import", new AntScriptFunction (ar) {
 			public Object body(Object[] args) {
 				String jar = "";
 				String classpath = "";
 				if(args.length == 1){
 					jar = "runtime.jar";
 					classpath = args[0].toString();
 				}  else {
 					jar = args[0].toString();
 					classpath = args[1].toString();
 				}
 				
 				this.core.loadJar(jar, classpath, false);
 				
 				return new Long(0);
 			}
 		});
 		
 		ar.addFunction("exec_file", new AntScriptFunction(ar) {
 			public Object body(Object[] args){
 				try {
 					this.core.runFile(args[0].toString());
 				} catch (AntScriptException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return new Long(0);
 			}
 		}); 
 		
 		ar.addFunction("exec", new AntScriptFunction(ar) {
 			public Object body(Object[] args){
 				try {
 					this.core.run(args[0].toString());
 				} catch (AntScriptException e) {
 					e.printStackTrace();
 				}
 				return new Long(0);
 			}
 		}); 
 		
 		ar.addFunction("quit", new AntScriptFunction(ar) {
 			public Object body(Object[] args){
 				if(args.length == 0)
 					System.exit(0);
 				else
 					System.exit(Integer.parseInt(args[0].toString()));
 				return new Long(0);
 			}
 		}); 
 		
 		ar.addFunction("print", new AntScriptFunction() {
 			public Object body(Object[] args){
 				System.out.println(args[0].toString());
 				return new Long(0);
 			}
 		}); 
 		ar.addFunction("input", new AntScriptFunction(ar) {
 			private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 			public Object body(Object[] args){
 				try {
 					return (Object)in.readLine();
 				} catch (IOException e) {
 					this.core.setErrorCode(AntScriptCore.ERR_IO);
 					return (Boolean)false;
 				}
 			}
 		}); 
 		ar.addFunction("error", new AntScriptFunction(ar) {
 			public Object body(Object[] args) {
 				System.err.println(args[0].toString());
 				this.core.setErrorCode(AntScriptCore.ERR_USER);
 				return new Long(0);
 			}
 		});
 		ar.addFunction("match", new AntScriptFunction(){
 			public Object body(Object[] args) {
				return (Boolean)(args[1].toString().matches(args[0].toString()));
 			}
 		});
 		ar.addFunction("replace", new AntScriptFunction(){
 			public Object body(Object[] args) {
 				return (String)(args[2].toString().replaceAll(args[0].toString(), args[1].toString()));
 			}
 		});
 		ar.addFunction("find", new AntScriptFunction(){
 			public Object body(Object[] args) {
 				Pattern p = Pattern.compile(args[0].toString());
 				Matcher m = p.matcher(args[1].toString());
 				if(m.find())  {
 					return m.group(0);
 				} else {
 					return (Boolean)false;
 				}
 			}
 		});
 		ar.addFunction("file_read", new AntScriptFunction(ar){
 			public Object body(Object[] args) {
 				String source = "";
 				try {
 					String tmp;
 					FileInputStream fis = new FileInputStream(args[0].toString());
 
 					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 					while((tmp = br.readLine()) != null){
 						source += tmp + '\n';
 					}
 					
 				} catch(Exception e) {
 					this.core.setErrorCode(AntScriptCore.ERR_IO);
 					return (Boolean)false;
 				}
 				return (Object)source;
 			}
 		});
 		ar.addFunction("file_write", new AntScriptFunction(ar){
 			public Object body(Object[] args) {
 				try {
 					FileWriter fstream = new FileWriter(args[0].toString());
 					BufferedWriter out = new BufferedWriter(fstream);
 					out.write(args[1].toString());
 					out.close();
 				} catch(Exception e) {
 					this.core.setErrorCode(AntScriptCore.ERR_IO);
 					return (Boolean)false;
 				}
 				return (Boolean)true;
 			}
 		});
 		ar.addFunction("sleep", new AntScriptFunction (ar) {
 			public Object body(Object[] args) {
 				try {
 					Thread.sleep((Long)args[0]);
 				} catch (InterruptedException e) {
 					this.core.setErrorCode(AntScriptCore.ERR_IO);
 				}
 				return new Long(0);
 			}
 		});
 		
 		ar.addFunction("http_get", new AntScriptFunction() {
 			public Object body(Object[] args) {
 				URL url;
 			    HttpURLConnection conn;
 			    BufferedReader rd;
 			    String line;
 			    String result = "";
 			    try {
 			       url = new URL(args[0].toString());
 			       conn = (HttpURLConnection) url.openConnection();
 			       conn.setRequestMethod("GET");
 			       rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			       while ((line = rd.readLine()) != null) {
 			          result += line;
 			       }
 			       rd.close();
 			    } catch (Exception e) {
 			       e.printStackTrace();
 			    }
 			    return result;
 			}
 		});
 		
 		ar.addFunction("http_post", new AntScriptFunction() {
 			public Object body(Object[] args) {
 				String rst = "";
 				try {
 				    // Construct data
 				    String data = args[1].toString();
 				    // Send data
 				    URL url = new URL(args[0].toString());
 				    URLConnection conn = url.openConnection();
 				    conn.setDoOutput(true);
 				    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 				    wr.write(data);
 				    wr.flush();
 
 				    // Get the response
 				    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 				    String line;
 				    while ((line = rd.readLine()) != null) {
 				        rst += line + "\n";
 				    }
 				    wr.close();
 				    rd.close();
 				} catch (Exception e) {
 				}
 				return rst;
 			}
 		});
 		
 		ar.addFunction("int", new AntScriptFunction (ar) {
 			public Object body(Object[] args) {
 				return new Long(args[0].toString());
 			}
 		});
 		ar.addFunction("str", new AntScriptFunction (ar) {
 			public Object body(Object[] args) {
 				return args[0].toString();
 			}
 		});
 	}
 }
