 package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dcode;
 
 import java.awt.Color;
 import java.awt.Insets;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments.CFLABEL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions.RLABEL;
 
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 
 public class DCode extends JTextArea implements Observer {
 
 	static final private String PATH_TO_REPOSITORY = Config
 			.getPATH_TO_REPOSITORY();
 	static final private String TARGET = Config.getTARGET();
 
 	static public final int TAB_SIZE = 4;
 
 	public final JScrollPane scrollPane;
 
 	private Long revision;
 	private CodeFragment codefragment;
 
 	public DCode() {
 
 		this.setTabSize(TAB_SIZE);
 
 		this.revision = null;
 		this.codefragment = null;
 
 		final Insets margin = new Insets(5, 50, 5, 5);
 		this.setMargin(margin);
 		this.setUI(new DCodeUI(new HashSet<Integer>(), this, margin));
 		this.setText("");
 
 		this.scrollPane = new JScrollPane();
 		this.scrollPane.setViewportView(this);
 		this.scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		this.scrollPane
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 		this.scrollPane.setBorder(new TitledBorder(new LineBorder(Color.black),
 				"Source Code View"));
 	}
 
 	@Override
 	public void update(final Observable o, final Object arg) {
 
 		if (o instanceof ObservedFiles) {
 			final ObservedFiles observedFiles = (ObservedFiles) o;
 			if (observedFiles.label.equals(FLABEL.SELECTED)) {
 
 				this.setText("");
 
 				if (observedFiles.isSet()) {
 
 					try {
 
 						final String path = observedFiles.get().first();
 
 						final SVNURL fileurl = SVNURL.fromFile(new File(
 								PATH_TO_REPOSITORY
 										+ System.getProperty("file.separator")
 										+ path));
 						final SVNWCClient wcClient = SVNClientManager
 								.newInstance().getWCClient();
 
 						final StringBuilder text = new StringBuilder();
 						wcClient.doGetFileContents(fileurl,
 								SVNRevision.create(this.revision.longValue()),
 								SVNRevision.create(this.revision.longValue()),
 								false, new OutputStream() {
 									@Override
 									public void write(int b) throws IOException {
 										text.append((char) b);
 									}
 								});
 
 						final List<Token> tokens = Token.getTokens(text
 								.toString());
 						final List<Statement> statements = Statement
 								.getStatements(tokens);
 						final SortedSet<Integer> highlightedLines = this
 								.getPositions(statements,
 										this.codefragment.statements);
 
 						final Insets margin = new Insets(5, 50, 5, 5);
 						this.setMargin(margin);
 						this.setUI(new DCodeUI(highlightedLines, this, margin));
 						this.setText(text.toString());
 
 					} catch (final Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		else if (o instanceof ObservedRevisions) {
 			final ObservedRevisions observedRevisions = (ObservedRevisions) o;
 			if (observedRevisions.label.equals(RLABEL.SELECTED)) {
 				if (observedRevisions.isSet()) {
 					this.revision = observedRevisions.get().first();
 				}
 			}
 		}
 
 		else if (o instanceof ObservedCodeFragments) {
 			final ObservedCodeFragments observedCodeFragments = (ObservedCodeFragments) o;
			if (observedCodeFragments.label.equals(CFLABEL.SELECTED)) {
 				if (observedCodeFragments.isSet()) {
 					this.codefragment = observedCodeFragments.get().first();
 				}
 			}
 		}
 	}
 
 	private SortedSet<Integer> getPositions(final List<Statement> statements,
 			final List<Statement> pattern) {
 
 		final SortedSet<Integer> lines = new TreeSet<Integer>();
 		final SortedSet<Integer> candidates = new TreeSet<Integer>();
 
 		int pIndex = 0;
 		for (int index = 0; index < statements.size(); index++) {
 
 			final Statement statement = statements.get(index);
 			if (statement.hash == pattern.get(pIndex).hash) {
 				pIndex++;
				candidates.add(statement.tokens.get(0).line - 1);
 				candidates
						.add(statement.tokens.get(statement.tokens.size() - 1).line - 1);
 				if (pIndex == pattern.size()) {
 					pIndex = 0;
 					lines.addAll(candidates);
 					candidates.clear();
 				}
 			}
 
 			else {
 				pIndex = 0;
 				candidates.clear();
 			}
 		}
 
 		return lines;
 	}
 }
