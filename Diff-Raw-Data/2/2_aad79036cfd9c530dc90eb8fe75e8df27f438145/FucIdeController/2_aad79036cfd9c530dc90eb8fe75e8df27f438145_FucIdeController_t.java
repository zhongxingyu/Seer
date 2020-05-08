 package swp_compiler_ss13.fuc.gui.ide;
 
 import java.awt.Component;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.WriterAppender;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.backend.Backend;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser;
 import swp_compiler_ss13.fuc.errorLog.LogEntry;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeButton;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeMenu;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeStatusLabel;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeTab;
 import swp_compiler_ss13.fuc.gui.ide.mvc.Controller;
 import swp_compiler_ss13.fuc.gui.ide.mvc.Position;
 
 /**
  * The FUC IDE Controllre
  * 
  * @author "Frank Zechert"
  * @version 1
  */
 public class FucIdeController {
 	/**
 	 * The model
 	 */
 	private FucIdeModel model;
 	/**
 	 * The view
 	 */
 	private FucIdeView view;
 
 	private static Logger logger = Logger.getLogger(FucIdeController.class);
 
 	/**
 	 * Instantiate a new instance of the controller
 	 */
 	public FucIdeController() {
 
 		this.redirectSystemStreams();
 		this.model = new FucIdeModel(this);
 		this.view = new FucIdeView(this);
 
 		this.initComponents();
 		this.setUpInitialState();
 
 		this.view.setVisible(true);
 	}
 
 	private void redirectSystemStreams() {
 		final StringWriter consoleWriter = new StringWriter();
 		WriterAppender appender = new WriterAppender(new PatternLayout("%d{ISO8601} %p - %m%n"), consoleWriter);
 		appender.setName("GUI_APPENDER");
 		appender.setThreshold(org.apache.log4j.Level.DEBUG);
 		Logger.getRootLogger().addAppender(appender);
 
 		new Thread() {
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						SwingUtilities.invokeAndWait(new Runnable() {
 							@Override
 							public void run() {
 								FucIdeController.this.updateTextPane(consoleWriter.toString());
 								consoleWriter.getBuffer().setLength(0);
 							}
 						});
 
 						Thread.sleep(500);
 					} catch (InvocationTargetException | InterruptedException e) {
 						// ignore
 					}
 				}
 			}
 		}.start();
 	}
 
 	protected void updateTextPane(String valueOf) {
 		if (this.view != null) {
 			this.view.updateTextPane(valueOf);
 		}
 	}
 
 	/**
 	 * Init the compiler copmonents
 	 */
 	private void initComponents() {
 		List<Lexer> lexers = this.model.getLexers();
 		for (Lexer lexer : lexers) {
 			this.view.addComponentRadioMenuItem(lexer);
 		}
 		List<Parser> parsers = this.model.getParsers();
 		for (Parser parser : parsers) {
 			this.view.addComponentRadioMenuItem(parser);
 		}
 		List<SemanticAnalyser> semanticAnalyzers = this.model.getSemanticAnalysers();
 		for (SemanticAnalyser sa : semanticAnalyzers) {
 			this.view.addComponentRadioMenuItem(sa);
 		}
 		List<IntermediateCodeGenerator> irgs = this.model.getIntermediateCodeGenerators();
 		for (IntermediateCodeGenerator irg : irgs) {
 			this.view.addComponentRadioMenuItem(irg);
 		}
 		List<Backend> backends = this.model.getBackends();
 		for (Backend backend : backends) {
 			this.view.addComponentRadioMenuItem(backend);
 		}
 
 		if (lexers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Lexer.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (parsers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Parser.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (semanticAnalyzers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							SemanticAnalyser.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (irgs.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							IntermediateCodeGenerator.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (backends.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Backend.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		List<Controller> cl = this.model.getGUIControllers();
 		for (Controller c : cl) {
 			logger.info("Initializing gui component " + c.getClass().getName());
 			c.init(this.model);
 			boolean notify = false;
 			notify = notify || c.getModel().setSourceCode("");
 			notify = notify || c.getModel().setTokens(null);
 			notify = notify || c.getModel().setAST(null);
 			notify = notify || c.getModel().setTAC(null);
 			notify = notify || c.getModel().setTargetCode(null);
 			if (notify) {
 				logger.info("notifying the controller " + c.getClass().getName() + " about model changes");
 				c.notifyModelChanged();
 			}
 			this.model.addTab(c);
 			this.notifyModelTab();
 		}
 	}
 
 	public void notifyModelAddedMenu() {
 		FucIdeMenu[] menus = this.model.getMenus().toArray(new FucIdeMenu[] {});
 		Arrays.sort(menus);
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearMenus();
 			}
 		});
 		for (final FucIdeMenu menu : menus) {
 
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addMenu(menu);
 				}
 			});
 		}
 	}
 
 	public void notifyModelAddedButton() {
 		FucIdeButton[] buttons = this.model.getButtons().toArray(new FucIdeButton[] {});
 		Arrays.sort(buttons);
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearButtons();
 			}
 		});
 		for (final FucIdeButton button : buttons) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addButton(button);
 				}
 			});
 		}
 	}
 
 	public void notifyModelAddedLabel() {
 		FucIdeStatusLabel[] labels = this.model.getLabels().toArray(new FucIdeStatusLabel[] {});
 		Arrays.sort(labels);
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearLabels();
 			}
 		});
 		for (final FucIdeStatusLabel label : labels) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addLabel(label);
 				}
 			});
 		}
 	}
 
 	public void notifyModelTab() {
 		FucIdeTab[] tabs = this.model.getTabs().toArray(new FucIdeTab[] {});
 		Arrays.sort(tabs);
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 
 				FucIdeController.this.view.clearTabs();
 			}
 		});
 		for (final FucIdeTab tab : tabs) {
 			logger.info("adding tab " + tab.getName());
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addTab(tab.getName(), tab.getComponent());
 				}
 			});
 		}
 
 	}
 
 	private void setUpInitialState() {
 		for (final FucIdeTab t : this.model.getTabs()) {
 			if (this.view.isFirstTab(t)) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						FucIdeController.this.updateStatus(t.getPosition());
 						FucIdeController.this.view.showFirstTab();
 					}
 
 				});
 				break;
 			}
 		}
 	}
 
 	private void updateStatus(Position position) {
 		logger.info("Showing the tab for position " + position);
 		FucIdeMenu[] menus = this.model.getMenus().toArray(new FucIdeMenu[] {});
 		Arrays.sort(menus);
 
 		this.view.clearMenus();
 		for (FucIdeMenu menu : menus) {
 			if (menu.isAlwaysVisible() || menu.getPosition() == position) {
 				this.view.addMenu(menu);
 				break;
 			}
 		}
 
 		FucIdeButton[] buttons = this.model.getButtons().toArray(new FucIdeButton[] {});
 		Arrays.sort(buttons);
 
 		this.view.clearButtons();
 		for (FucIdeButton button : buttons) {
 			if (button.isAlwaysVisible() || button.getPosition() == position) {
 				this.view.addButton(button);
 			}
 		}
 
 		FucIdeStatusLabel[] labels = this.model.getLabels().toArray(new FucIdeStatusLabel[] {});
 		Arrays.sort(labels);
 
 		this.view.clearLabels();
 		for (FucIdeStatusLabel label : labels) {
 			if (label.isAlwaysVisible() || label.getPosition() == position) {
 				this.view.addLabel(label);
 			}
 		}
 
 		this.view.invalidate();
 
 	}
 
 	public void tabChanged() {
 		for (final FucIdeTab t : this.model.getTabs()) {
 			if (this.view.isCurrentTab(t)) {
 				this.model.setActiveTab(t);
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						FucIdeController.this.updateStatus(t.getPosition());
 					}
 				});
 				break;
 			}
 		}
 
 		this.view.invalidate();
 	}
 
 	public static void main(String[] args) {
 		new FucIdeController();
 	}
 
 	public void onRunPressed() {
 		Lexer lexer = this.model.getActiveLexer();
 		Parser parser = this.model.getActiveParser();
 		SemanticAnalyser analyzer = this.model.getActiveAnalyzer();
 		IntermediateCodeGenerator irgen = this.model.getActiveIRG();
 		Backend backend = this.model.getActiveBackend();
 
 		String sourceCode = this.model.getSourceCode();
 		if (sourceCode == null) {
 			sourceCode = "";
 		}
 		final ReportLogImpl log = new ReportLogImpl();
 
 		try {
 			// do lexer stuff
 			lexer = lexer.getClass().newInstance();
 
 			lexer.setSourceStream(new ByteArrayInputStream(sourceCode.getBytes()));
 			List<Token> tokens = new LinkedList<>();
 			while (true) {
 				Token t = lexer.getNextToken();
 				if (t.getTokenType() == TokenType.EOF) {
 					break;
 				}
 				tokens.add(t);
 			}
 			for (Controller c : this.model.getGUIControllers()) {
 				if (c.getModel().setTokens(tokens)) {
 					c.notifyModelChanged();
 				}
 			}
 
 			// do parser stuff
 			lexer = lexer.getClass().newInstance();
 			lexer.setSourceStream(new ByteArrayInputStream(sourceCode.getBytes()));
 			parser = parser.getClass().newInstance();
 			parser.setLexer(lexer);
 			parser.setReportLog(log);
 			AST ast = parser.getParsedAST();
 			for (Controller c : this.model.getGUIControllers()) {
 				if (c.getModel().setAST(ast)) {
 					c.notifyModelChanged();
 				}
 			}
 
 			if (ast != null) {
 
 				// do analyzer stuff
 				analyzer = analyzer.getClass().newInstance();
 				analyzer.setReportLog(log);
 				AST checkedAST = analyzer.analyse(ast);
 
 				// do irgen stuff
 				irgen = irgen.getClass().newInstance();
 				List<Quadruple> tac = irgen.generateIntermediateCode(checkedAST);
 				for (Controller c : this.model.getGUIControllers()) {
 					if (c.getModel().setTAC(tac)) {
 						c.notifyModelChanged();
 					}
 				}
 
 				// do backend stuff
 				backend = backend.getClass().newInstance();
 				Map<String, InputStream> target = backend.generateTargetCode("Program", tac);
 				for (Controller c : this.model.getGUIControllers()) {
 					if (c.getModel().setTargetCode(target)) {
 						c.notifyModelChanged();
 					}
 				}
 			}
 
 		} catch (Throwable e) {
 			new FucIdeCriticalError(this.view, e, true);
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 
 				FucIdeController.this.view.clearErrorLog();
 				int e = 0;
				for (LogEntry error : log.getEntries()) {
 					String warnOrError = error.getLogType().toString();
 					String message = error.getMessage();
 					String type = error.getReportType().toString();
 					String tokens = (null == error.getTokens()) ? "null" : error.getTokens().toString();
 
 					FucIdeController.this.view.addErrorLog(warnOrError + " - " + type);
 					FucIdeController.this.view.addErrorLog(message);
 					FucIdeController.this.view.addErrorLog(tokens);
 					FucIdeController.this.view.addErrorLog("");
 					e++;
 				}
 				if (e == 0) {
 					FucIdeController.this.view.addErrorLog("No errors reported");
 				}
 			}
 		});
 	}
 
 	public void onLexerSelected(Lexer lexer) {
 		logger.info("Lexer component active: " + lexer.getClass().getName());
 		this.model.setActiveLexer(lexer);
 	}
 
 	public void onParserSelected(Parser parser) {
 		logger.info("Parser component active: " + parser.getClass().getName());
 		this.model.setActiveParser(parser);
 	}
 
 	public void onAnalyzerSelected(SemanticAnalyser analyzer) {
 		logger.info("SemanticAnalyzer component active: " + analyzer.getClass().getName());
 		this.model.setActiveAnalyzer(analyzer);
 	}
 
 	public void onIRGSelected(IntermediateCodeGenerator irgen) {
 		logger.info("IntermediateCodeGenerator component active: " + irgen.getClass().getName());
 		this.model.setActiveIRG(irgen);
 	}
 
 	public void onBackendSelected(Backend backend) {
 		logger.info("Backend component active: " + backend.getClass().getName());
 		this.model.setActiveBackend(backend);
 	}
 
 	public void notifySourceCodeChanged() {
 		logger.info("Source code was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setSourceCode(this.model.getSourceCode())) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public Component getView() {
 		return this.view;
 	}
 }
