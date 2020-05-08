 package pleocmd.pipe.out;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import pleocmd.Log;
 import pleocmd.exc.OutputException;
 import pleocmd.pipe.Config;
 import pleocmd.pipe.ConfigEnum;
 import pleocmd.pipe.ConfigPath;
 import pleocmd.pipe.Data;
 
 public final class FileOutput extends Output {
 
 	private File file;
 
 	private PrintType type;
 
 	private DataOutputStream out;
 
 	public FileOutput() {
 		super(new Config().addV(
 				new ConfigPath("File", ConfigPath.PathType.FileForWriting))
 				.addV(new ConfigEnum("PrintType", PrintType.values())));
 	}
 
 	@Override
 	protected void configured0() {
 		file = new File(((ConfigPath) getConfig().get(0)).getContent());
 		type = PrintType.values()[((ConfigEnum) getConfig().get(1))
 				.getContent()];
 	}
 
 	@Override
 	protected void init0() throws IOException {
 		Log.detail("Opening file '%s' for output of type %s", file, type);
 		out = new DataOutputStream(new FileOutputStream(file));
 	}
 
 	@Override
 	protected void close0() throws IOException {
 		Log.detail("Closing file '%s'", file);
 		out.close();
 		out = null;
 	}
 
 	@Override
 	protected void write0(final Data data) throws OutputException, IOException {
 		switch (type) {
 		case DataBinary:
 			data.writeToBinary(out);
 			break;
 		case DataAscii:
			data.writeToAscii(out, true);
 			break;
 		case PleoMonitorCommands:
 			if ("PMC".equals(data.getSafe(0).asString()))
 				Log.consoleOut(data.getSafe(1).asString());
 			break;
 		default:
 			throw new OutputException(this, true,
 					"Internal error: Invalid print-type: %s", type);
 		}
 	}
 
 }
