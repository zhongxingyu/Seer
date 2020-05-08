 package service.command.impl;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.util.Scanner;
 
 import model.configuration.Config;
 import model.util.IOUtil;
 import service.AbstractSockectService;
 import service.StatusCodes;
 import service.command.ServiceCommand;
 
 public class DeleteLineCommand extends ServiceCommand {
 
 	public DeleteLineCommand(AbstractSockectService owner) {
 		super(owner);
 	}
 
 	@Override
 	public void execute(String[] params) throws IOException {
 		if (params.length < 2) {
 			owner.echoLine(StatusCodes.ERR_INVALID_PARAMETERS_ARGUMENTS);
 			return;
 		}
 		int lineNumberToRemove;
 		try {
 			lineNumberToRemove = Integer.valueOf(params[0]);
 		} catch (NumberFormatException e) {
 			owner.echoLine(StatusCodes.ERR_INVALID_PARAMETERS_NUMBER,  "input: " + params[0]);
 			return;
 		}
 		String path = Config.getInstance().getConfigResourcePath(params[1]);
 		if (path == null) {
 			owner.echoLine(StatusCodes.ERR_INVALID_PARAMETERS_FILE);
 			return;
 		}
 		StringBuilder text = getTextWithoutLine(path, lineNumberToRemove);
 		try {
 			String rootPath = IOUtil.getRoot();
 			rootPath = rootPath.substring(0, rootPath.lastIndexOf('/') + 1);
 			PrintWriter out = new PrintWriter(rootPath + path);
 			out.print(text.toString());
 			out.close();
 			owner.echoLine(StatusCodes.OK_FILE_UPDATED);
 			Config.getInstance().update(params[1]);
 		} catch (FileNotFoundException e) {
			owner.echoLine(StatusCodes.ERR_INVALID_PARAMETERS_FILE);
 		} catch (URISyntaxException e) {
 			owner.echoLine(StatusCodes.ERR_INVALID_PARAMETERS_FILE);
 		}
 	}
 	
 	public StringBuilder getTextWithoutLine(String path, int lineNumber) throws IOException {
 		StringBuilder modifiedFile = new StringBuilder();
 		Scanner scanner = new Scanner(IOUtil.getStream(path));
 		int currLine = 1;
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (currLine != lineNumber) {
 				modifiedFile.append(line + "\r\n");
 			}
 			currLine++;
 		}
 		scanner.close();
 		return modifiedFile;
 	}
 
 }
