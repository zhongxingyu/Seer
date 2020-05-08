 package us.jubat.common;
 
 import java.net.UnknownHostException;
 import java.util.Map;
 
 import org.msgpack.rpc.Client;
 import org.msgpack.rpc.Future;
 import org.msgpack.rpc.error.RemoteError;
 import org.msgpack.rpc.loop.EventLoop;
 import org.msgpack.type.Value;
 
 import us.jubat.common.type.TBool;
 import us.jubat.common.type.TMap;
 import us.jubat.common.type.TString;
 import us.jubat.common.type.TType;
 
 public class ClientBase {
 	private Client client;
 	protected String name;
 
 	public ClientBase(String host, int port, String name, int timeoutSec)
 			throws UnknownHostException {
 		EventLoop loop = EventLoop.defaultEventLoop();
 		this.client = new Client(host, port, loop);
 		this.client.setRequestTimeout(timeoutSec);
 		this.name = name;
 	}
 
 	public <T> T call(String method, TType<T> returnType, Object... args) {
 		Object[] arguments = new Object[args.length + 1];
 		arguments[0] = this.name;
 		for (int i = 0; i < args.length; ++i) {
 			arguments[i + 1] = args[i];
 		}
 		Future<Value> future = this.client.callAsyncApply(method, arguments);
 		try {
 			future.join();
 			if (future.getError() != null) {
 				throw translateError(future.getError());
 			} else {
 				Value result = future.get();
 				return returnType.revert(result);
 			}
 		} catch (InterruptedException e) {
 			// TODO
 			throw new RemoteError();
 		}
 	}
 
 	public String getConfig() {
 		return this.call("get_config", TString.instance);
 	}
 
 	public boolean save(String id) {
 		TString.instance.check(id);
 		return this.call("save", TBool.instance, id);
 	}
 
 	public boolean load(String id) {
 		TString.instance.check(id);
 		return this.call("load", TBool.instance, id);
 	}
 
 	public Map<String, Map<String, String>> getStatus() {
 		return this.call(
 				"get_status",
 				TMap.create(TString.instance,
 						TMap.create(TString.instance, TString.instance)));
 	}
 
 	public Client getClient() {
 		return client;
 	}
 
 	private RuntimeException translateError(Value error) {
 		if (error.isIntegerValue()) {
 			int value = error.asIntegerValue().getInt();
 			if (value == 1) {
 				return new UnknownMethod();
 			} else if (value == 2) {
 				return new TypeMismatch();
 			}
 		}
		return null;
 	}
 }
