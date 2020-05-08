 package org.jikespg.uide.compare;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.IEditableContent;
 import org.eclipse.compare.IEncodedStreamContentAccessor;
 import org.eclipse.compare.IResourceProvider;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.IViewerCreator;
 import org.eclipse.compare.structuremergeviewer.IStructureComparator;
 import org.eclipse.compare.structuremergeviewer.IStructureCreator;
 import org.eclipse.compare.structuremergeviewer.StructureDiffViewer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.uide.parser.ILexer;
 import org.eclipse.uide.parser.IParser;
 import org.eclipse.uide.utils.StreamUtils;
 import org.jikespg.uide.parser.JikesPGLexer;
 import org.jikespg.uide.parser.JikesPGParser;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 
 public class StructureMergeViewerCreator implements IViewerCreator {
     private static class JikesPGStructureCreator implements IStructureCreator {
         private final CompareConfiguration fConfig;
 
         public JikesPGStructureCreator(CompareConfiguration config) {
             fConfig= config;
         }
 
         public String getName() {
             return "JikesPG Grammar Structure Compare";
         }
 
         public IStructureComparator getStructure(Object input) {
             try {
                 IResource res= null;
                 IDocument doc= CompareUI.getDocument(input);
                 IStreamContentAccessor sca;
 
                 if (input instanceof IResourceProvider) {
                     res= ((IResourceProvider) input).getResource();
                 } else
                     return null;
 
                 if (input instanceof IStreamContentAccessor) {
                     sca= (IStreamContentAccessor) input;
                 } else
                     return null;
 
                 if (doc == null) { // Set up a document
                     String contents= StreamUtils.readStreamContents(sca);
                     char[] buffer= null;
                         
                     if (contents != null) {
                         int n= contents.length();
                         buffer= new char[n];
                         contents.getChars(0, n, buffer, 0);
                                 
                         doc= new Document(contents);
                         CompareUI.registerDocument(input, doc);
                     }
                 }
 
                 return new JikesPGStructureNode(parseStream(sca, res.getFullPath()), doc, 0, "root");
             } catch (IOException io) {
             } catch (CoreException ce) {
             }
             return null;
         }
 
         private ASTNode parseFile(IFile file) throws IOException, CoreException {
             String contents= StreamUtils.readStreamContents(file.getContents(), file.getCharset());
 
             return parseContents(contents, file.getLocation());
         }
 
         private ASTNode parseStream(IStreamContentAccessor sca, IPath path) throws IOException, CoreException {
             String contents= StreamUtils.readStreamContents(sca);
 
             return parseContents(contents, path);
         }
 
         private ASTNode parseContents(String contents, IPath path) {
             char[] contentsArray= contents.toCharArray();
             ILexer lexer= new JikesPGLexer();
             IParser parser= new JikesPGParser(lexer.getLexStream());
 
             lexer.initialize(contentsArray, path.toString());
             parser.getParseStream().resetTokenStream();
             lexer.lexer(null, parser.getParseStream()); // Lex the stream to produce the token stream
             return (ASTNode) parser.parser(null, 0);
         }
 
         public IStructureComparator locate(Object path, Object input) {
             JikesPGStructureNode inputAST= (JikesPGStructureNode) input;
 
             // TODO Figure out when/why this is called and implement :-)
             return null;
         }
 
         public String getContents(Object node, boolean ignoreWhitespace) {
             JikesPGStructureNode wrapper= (JikesPGStructureNode) node;
 
            return wrapper.getASTNode().toString();
         }
 
         public void save(IStructureComparator node, Object input) {
             if (node instanceof JikesPGStructureNode && input instanceof IEditableContent) {
                 IDocument document= ((JikesPGStructureNode) node).getDocument();
                 IEditableContent bca= (IEditableContent) input;
                 String contents= document.get();
                 String encoding= null;
 
                 if (input instanceof IEncodedStreamContentAccessor) {
                     try {
                         encoding= ((IEncodedStreamContentAccessor)input).getCharset();
                     } catch (CoreException e1) {
                         // ignore
                     }
                 }
                 if (encoding == null)
                     encoding= ResourcesPlugin.getEncoding();
                 byte[] bytes;                           
                 try {
                     bytes= contents.getBytes(encoding);
                 } catch (UnsupportedEncodingException e) {
                     bytes= contents.getBytes();     
                 }
                 bca.setContent(bytes);
             }
         }
     }
 
     private static final class JikesPGStructureDiffViewer extends StructureDiffViewer {
         private IStructureCreator fStructureCreator;
 
         private JikesPGStructureDiffViewer(Composite parent, CompareConfiguration config) {
             super(parent, config);
             fStructureCreator= new JikesPGStructureCreator(config);
             setStructureCreator(fStructureCreator);
         }
     }
 
     public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
         return new JikesPGStructureDiffViewer(parent, config);
     }
 }
