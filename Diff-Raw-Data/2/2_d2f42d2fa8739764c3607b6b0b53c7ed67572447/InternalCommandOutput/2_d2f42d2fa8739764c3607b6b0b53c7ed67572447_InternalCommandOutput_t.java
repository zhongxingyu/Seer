 package pleocmd.pipe.out;
 
 import java.io.IOException;
 
 import pleocmd.exc.OutputException;
 import pleocmd.pipe.Config;
 import pleocmd.pipe.Data;
 
public final class InternalCommandOutput extends Output {
 
 	public InternalCommandOutput() {
 		super(new Config());
 	}
 
 	@Override
 	protected void configure0() throws OutputException, IOException {
 		// nothing to do
 	}
 
 	@Override
 	protected void init0() throws OutputException, IOException {
 		// nothing to do
 	}
 
 	@Override
 	protected void close0() throws OutputException, IOException {
 		// nothing to do
 	}
 
 	@Override
 	protected void write0(final Data data) throws OutputException, IOException {
 		if ("SC".equals(data.getSafe(0).asString())) {
 			final String v2 = data.get(1).asString();
 			if ("SLEEP".equals(v2))
 				try {
 					Thread.sleep(data.getSafe(2).asLong());
 				} catch (final InterruptedException e) {
 					throw new OutputException(this, false, e,
 							"Could not execute special command '%s'", data);
 				}
 		}
 	}
 }
