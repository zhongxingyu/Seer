 import java.util.*;
 import java.io.*;
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.util.cli.OutputStrings;
 import org.jruby.RubyClass;
 import org.jruby.RubyModule;
 import org.jruby.RubyArray;
 import org.jruby.RubyObject;
 import org.jruby.RubyHash;
 import org.jruby.RubyString;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.BlockCallback;
 import org.jruby.runtime.CallBlock;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.Block;
 import org.jruby.runtime.Arity;
 import org.jruby.anno.JRubyClass;
 import org.jruby.anno.JRubyMethod;
 import org.jruby.runtime.ObjectAllocator;
import org.jcodings.Encoding;
 
 	
 public class jwsgi_rack {
 
 	// the ruby instance
 	Ruby rb;
 	// the rack application
 	RubyObject app;
 	// the rack.input class
 	RubyClass rack_input;
 
 	@JRubyClass(name="Uwsgi_IO")
 	public class RackInput extends RubyObject {
 
 		public uwsgi.RequestBody input;
 
 		public RackInput(final Ruby ruby, RubyClass rubyClass) {
 			super(ruby, rubyClass);
 		}
 
 		@JRubyMethod(name="gets")
 		public IRubyObject gets(ThreadContext context) {
 			// unimplemented
			System.out.println("CALLING GETS");
 			return context.nil;
 		}
 
 		@JRubyMethod(name="each")
 		public IRubyObject each(ThreadContext context) {
 			// unimplemented
			System.out.println("CALLING EACH");
 			return context.nil;
 		}
 
 		@JRubyMethod(name="rewind")
                 public IRubyObject rewind(ThreadContext context) {
 			input.seek(0);
 			return context.nil;
 		}
 
 		@JRubyMethod(name="read",optional=2)
 		public IRubyObject read(ThreadContext context, IRubyObject [] args) {
 			long want = 0;
 			boolean return_nil = false;

 			// read all
 			if (args.length == 0) {
 				want = input.available();
 			}
 			else {
 				if (args[0] == context.getRuntime().getNil()) {
 					want = input.available();
 				}
 				else {
 					want = args[0].convertToInteger().getLongValue();
 					if (want == 0) {
 						want = input.available();
 					}
 					else {
 						return_nil = true;
 					}
 				}
 			}
 
 			byte[] b = new byte[(int)want];
 			int ret = input.read(b);
 			if (ret > 0) {
 				if (args.length > 1) {
 					RubyString buf = (RubyString) args[1];
 					buf.resize(ret);
 					buf.cat(b, 0, ret);
 					return buf;
 				}
				return RubyString.newString(context.getRuntime(), b, 0, ret);
 			}
 			if (return_nil) {
 				return context.nil;
 			}
			RubyString rs_ret = context.getRuntime().newString("");
			return rs_ret;
 		}
 	}
 
 	static class HeadersBlock implements BlockCallback {
 		HashMap<String,Object> headers;
 
 		public HeadersBlock(HashMap<String,Object> h) {
 			headers = h;
 		}
                 public IRubyObject call(ThreadContext context, IRubyObject[] args, Block block) {
 			RubyArray arg = (RubyArray) args[0];
 			String h_key = (String) arg.get(0);
 			String h_value = (String) arg.get(1);
 			String[] values = h_value.split("\n"); 
 			for(int i=0;i<values.length;i++) {	
 				headers.put(h_key, values[i]);
 			}
                         return null;
                 }
         }
 
 	static class BodyBlock implements BlockCallback {
 		List<String> body_list;
 
                 public BodyBlock(List<String> bl) {
                         body_list = bl;
                 }
                 public IRubyObject call(ThreadContext context, IRubyObject[] args, Block block) {
                         RubyString arg = (RubyString) args[0];
 			body_list.add(arg.asJavaString());
                         return null;
                 }
         }
 	
 	public jwsgi_rack() throws Exception {
 		// check for the virtual option
 		if (!uwsgi.opt.containsKey("jwsgi-rack")) {
 			throw new Exception("you have to specify the path of a rack application with the \"jwsgi-rack\" virtual option");	
 		}
 		System.out.println("launching jruby...");
 		RubyInstanceConfig config = new RubyInstanceConfig();
 		rb = Ruby.newInstance();
 
 		config.getOutput().println(OutputStrings.getVersionString(config.getCompatVersion()));
 
 		rb.getLoadService().require("rubygems");
 		if (uwsgi.opt.containsKey("jwsgi-rack-bundler")) {
 			rb.getObject().callMethod("require", rb.newString("bundler/setup"));
 		}
 		rb.getObject().callMethod("require", rb.newString("rack"));
 
 		RubyModule rack_module = rb.getModule("Rack");
 		if (rack_module == null) {
 			throw new Exception("unable to find Rack::Builder");
 		}
 		RubyClass rack_builder = rack_module.getClass("Builder");
 		if (rack_builder == null) {
 			throw new Exception("unable to find Rack::Builder");
 		}
 		RubyArray rackup = (RubyArray) rack_builder.callMethod("parse_file", rb.newString((String)uwsgi.opt.get("jwsgi-rack")));
 		app = (RubyObject) rackup.get(0);
 
 		// we use Uwsgi_IO to be consistent with uWSGI rack plugin
                 rack_input = rb.defineClass("Uwsgi_IO", rb.getObject(), new ObjectAllocator() {
                         public IRubyObject allocate(Ruby ruby, RubyClass klazz) {
                                 return new RackInput(ruby, klazz);
                         }
                 });
 		// attach methods
 		rack_input.defineAnnotatedMethods(RackInput.class);
 	}
 
 	public Object[] application(HashMap env) throws Exception {
 
 		IRubyObject []no_args = { };
 
 		// fill the environ
 		RubyHash rb_env = new RubyHash(rb);
 		Iterator it = env.entrySet().iterator();
 		while(it.hasNext()) {
 			Map.Entry entry = (Map.Entry)it.next();
 			Object o_key = entry.getKey();
 			Object o_value = entry.getValue();
 			if (o_key instanceof String && o_value instanceof String) {
 				String str_key = (String) o_key;
 				String str_value = (String) o_value;
 				if (str_value.length() > 0 ||
 					str_key.equals("REQUEST_METHOD") ||
 					str_key.equals("SCRIPT_NAME") ||
 					str_key.equals("PATH_INFO") ||
 					str_key.equals("QUERY_STRING") ||
 					str_key.equals("SERVER_NAME") ||
 					str_key.equals("SERVER_PORT")) {
 					RubyString key = rb.newString( str_key );
 					RubyString value = rb.newString( str_value );
 					rb_env.put(key, value);
 				}
 			}
 		}
 
 		RackInput ri = (RackInput) rack_input.callMethod("new");
 		ri.input = (uwsgi.RequestBody) env.get("jwsgi.input");	
 		rb_env.put( rb.newString("rack.input"), ri);
 		rb_env.put( rb.newString("rack.errors"), rb.getGlobalVariables().get("$stderr"));
 		rb_env.put( rb.newString("rack.multithread"), rb.getTrue());
 		rb_env.put( rb.newString("rack.multiprocess"), rb.getTrue());
 		rb_env.put( rb.newString("rack.run_once"), rb.getFalse());
 		rb_env.put( rb.newString("rack.version"), rb.newArray(rb.newFixnum(1), rb.newFixnum(1)));
 		if (env.containsKey("UWSGI_SCHEME")) {
 			rb_env.put( rb.newString("rack.url_scheme"), rb_env.get(rb.newString("UWSGI_SCHEME")));
 		}
 		else if (env.containsKey("HTTPS")) {
 			rb_env.put( rb.newString("rack.url_scheme"), rb.newString("https"));
 		}
 		else {
 			rb_env.put( rb.newString("rack.url_scheme"), rb.newString("http"));
 		}
 		
 
 		// call the Rack function
 		RubyArray rb_response = (RubyArray) app.callMethod(rb.getCurrentContext(), "call", rb_env);
 
 		// get the status
 		long status = (Long) rb_response.get(0);
 
 		// get the headers
 		RubyObject rb_headers = (RubyObject) rb_response.get(1);
 		if (!rb_headers.respondsTo("each")) {
 			throw new Exception("Rack headers object must respond to \"each\"");
 		}
 		HashMap<String,Object> headers = new HashMap<String,Object>();
 		HeadersBlock hb = new HeadersBlock(headers);
 		Block headers_iterator = CallBlock.newCallClosure(rb_headers, null, Arity.ONE_ARGUMENT, hb, rb.getCurrentContext());
 		rb_headers.callMethod(rb.getCurrentContext(), "each", no_args, headers_iterator);
 
 		// get the body
 		Object generic_body = null;
 		RubyObject body = (RubyObject) rb_response.get(2);
 		// use sendfile()
 		if (body.respondsTo("to_path")) {
 			RubyString rb_path = (RubyString) body.callMethod(rb.getCurrentContext(), "to_path");
 			generic_body = new File(rb_path.asJavaString());	
 		}
 		// normal string list
 		else if (body.respondsTo("each")) {
 			List<String> body_list = new ArrayList<String>();
 			BodyBlock bb = new BodyBlock(body_list);
 			Block body_iterator = CallBlock.newCallClosure(body, null, Arity.ONE_ARGUMENT, bb, rb.getCurrentContext());	
 			body.callMethod(rb.getCurrentContext(), "each", no_args, body_iterator);
 			generic_body = body_list;
 		}
 
 		// call it always (if available)
 		if (body.respondsTo("close")) {
 			body.callMethod(rb.getCurrentContext(), "close");
 		}
 
 		// return the JWSGI response
         	Object[] response = { status, headers, generic_body };
         	return response;
 	}
 }
