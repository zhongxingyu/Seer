 /*$Id$*/
 package at.jku.snippetmaker;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 
 import at.jku.snippetmaker.Snippets.SnippetStep;
 
 public final class File extends SnippetTask {
 
 	private Type type = Type.html;
 	private java.io.File file = new java.io.File("snippets.html");
 	private StepFilter stepFilter;
 
 
 	public void setType(final Type type) {
 		this.type = type;
 	}
 
 	public void setFile(final java.io.File file) {
 		this.file = file;
 	}
 
 	public void addStepFilter(final StepFilter filter) {
 		this.stepFilter = filter;
 	}
 
 	@Override
 	public void execute() throws BuildException {
 		assert (this.type != null);
 		assert (this.file != null);
 
 		try {
 			final Properties p = new Properties();
 			try {
 				p.load(this.getClass().getResourceAsStream("/renderer-" + this.type.name() + ".properties"));
 			} catch (final IOException e) {
 				this.log("invalid renderer " + this.type, e, Project.MSG_ERR);
 				return;
 			}
 			final Snippets snippets = this.parseSnippets();
 
 
 			final String endl = System.getProperty("line.separator");
 			final MessageFormat head = new MessageFormat(p.getProperty("file.begin"));
 			final MessageFormat tail = new MessageFormat(p.getProperty("file.end"));
 			final MessageFormat begin = new MessageFormat(p.getProperty("step.begin"));
 			final MessageFormat between = new MessageFormat(p.getProperty("step.between"));
 			final MessageFormat sinsert = new MessageFormat(p.getProperty("snippet.insert"));
 			final MessageFormat sremove = new MessageFormat(p.getProperty("snippet.remove"));
 			final MessageFormat sfromto = new MessageFormat(p.getProperty("snippet.fromto"));
 			final MessageFormat sbetween = new MessageFormat(p.getProperty("snippet.between"));
 
 			PrintWriter writer = null;
 			try {
 				writer = new PrintWriter(this.file);
 
 				writer.append(head.format(this.asArgs(endl)));
 				for (final SnippetStep step : snippets) {
 					if (this.stepFilter != null && !this.stepFilter.include(step.getStep()))
 						continue;
 					writer.append(begin.format(this.asArgs(endl, step.getStep())));
 					for (final Snippet snippet : step) {
 						final String fullStep = step.getStep() + "." + snippet.getSubStep();
 						switch (snippet.getAction()) {
 						case INSERT:
							writer.append(sinsert.format(this.asArgs(endl, fullStep, snippet.getDescription(), this.transformCode(snippet.getCodeToInsert()))));
 							break;
 						case REMOVE:
							writer.append(sremove.format(this.asArgs(endl, fullStep, snippet.getDescription(), this.transformCode(snippet.getCodeToRemove()))));
 							break;
 						case FROM_TO:
							writer.append(sfromto.format(this.asArgs(endl, fullStep, snippet.getDescription(), this.transformCode(snippet.getCodeToRemove()),
									this.transformCode(snippet.getCodeToInsert()))));
 							break;
 						}
 						writer.append(sbetween.format(this.asArgs(endl)));
 					}
 					writer.append(between.format(this.asArgs(endl)));
 				}
 				writer.append(tail.format(this.asArgs(endl)));
 			} catch (final IOException e) {
 				this.log("can't create snippet file: " + this.file, e, Project.MSG_ERR);
 			} finally {
 				if (writer != null)
 					writer.close();
 			}
 		} catch (final Throwable e) {
 			e.printStackTrace();
 			this.log("error: " + e.getMessage(), e, Project.MSG_ERR);
 		}
 	}
 
	private String transformCode(final String code) {
		if (this.type == Type.html) {
			return code.replace("<", "&lt;").replace(">", "&gt;");
		} else
			return code;
	}

 	private Object[] asArgs(final Object... args) {
 		return args;
 	}
 
 	public static enum Type {
 		html, txt, latex
 	}
 }
