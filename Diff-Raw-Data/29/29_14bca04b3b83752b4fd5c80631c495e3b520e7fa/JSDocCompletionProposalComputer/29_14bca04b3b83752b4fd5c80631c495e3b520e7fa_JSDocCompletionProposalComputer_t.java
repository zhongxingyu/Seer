 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.javascript.core.JSKeywordCategory;
 import org.eclipse.dltk.javascript.core.JSKeywordManager;
 import org.eclipse.dltk.javascript.core.JavaScriptNature;
 import org.eclipse.dltk.javascript.internal.core.codeassist.JSCompletionEngine;
 import org.eclipse.dltk.javascript.internal.ui.JavaScriptUI;
 import org.eclipse.dltk.javascript.internal.ui.templates.JSDocTemplateCompletionProcessor;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.javascript.typeinfo.TypeMode;
import org.eclipse.dltk.javascript.ui.text.IJavaScriptPartitions;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.templates.ScriptTemplateProposal;
 import org.eclipse.dltk.ui.text.completion.ContentAssistInvocationContext;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposalComputer;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 
 @SuppressWarnings("restriction")
 public class JSDocCompletionProposalComputer implements
 		IScriptCompletionProposalComputer {
 
 	public JSDocCompletionProposalComputer() {
 	}
 
 	public void sessionStarted() {
 	}
 
 	public void sessionEnded() {
 	}
 
 	public List<ICompletionProposal> computeCompletionProposals(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		IDocument document = context.getDocument();
 		try {
			final IRegion region = getLineRegion(document,
					context.getInvocationOffset());
 			final char[] line = document.get(region.getOffset(),
 					region.getLength()).toCharArray();
 			final int offsetInLine = context.getInvocationOffset()
 					- region.getOffset();
 			int index = 0;
 			index = skipSpaces(line, index, offsetInLine);
 			if (index < offsetInLine && line[index] == '/') {
 				++index;
 			}
 			while (index < offsetInLine && line[index] == '*') {
 				++index;
 			}
 			index = skipSpaces(line, index, offsetInLine);
 			if (!(index < offsetInLine && line[index] == '@')) {
 				return Collections.emptyList();
 			}
 			final int tagStart = index;
 			++index;
 			if (index < offsetInLine
 					&& Character.isJavaIdentifierStart(line[index])) {
 				++index;
 				while (index < offsetInLine
 						&& (Character.isJavaIdentifierPart(line[index])
 								|| line[index] == '.' || line[index] == '-')) {
 					++index;
 				}
 			}
 			if (index == offsetInLine) {
 				return completionOnTag(context, new String(line, tagStart,
 						index - tagStart));
 			}
 			final int tagEnd = index;
 			index = skipSpaces(line, index, offsetInLine);
 			int depth = 0;
 			int nameStart = index;
 			boolean breakOnSpace = false;
 			if (index < offsetInLine && line[index] == '{') {
 				++index;
 				depth = 1;
 				nameStart = index;
 			} else if (JSDocTag.TYPE.equals(new String(line, tagStart, tagEnd
 					- tagStart))) {
 				breakOnSpace = true;
 			} else {
 				return Collections.emptyList();
 			}
 			while (index < offsetInLine) {
 				if (line[index] == '}') {
 					if (--depth <= 0) {
 						return Collections.emptyList();
 					}
 				} else if (line[index] == '{') {
 					++depth;
 				} else if (line[index] == '<' || line[index] == '>'
 						|| line[index] == ',' || line[index] == '|') {
 					nameStart = index + 1;
 				} else if (breakOnSpace && Character.isWhitespace(line[index])) {
 					return Collections.emptyList();
 				}
 				++index;
 			}
 			if (index == offsetInLine) {
 				return completionOnType(context, new String(line, nameStart,
 						index - nameStart));
 			}
 		} catch (BadLocationException e) {
 			JavaScriptUI.log(e);
 		}
 		return Collections.emptyList();
 	}
 
	private IRegion getLineRegion(IDocument document, int offset)
			throws BadLocationException {
		final IRegion region = document.getLineInformationOfOffset(offset);
		final ITypedRegion partition = TextUtilities.getPartition(document,
				IJavaScriptPartitions.JS_PARTITIONING, offset, false);
		if (partition.getOffset() > region.getOffset()
				|| partition.getOffset() + partition.getLength() < region
						.getOffset() + region.getLength()) {
			final int newOffset = Math.max(partition.getOffset(),
					region.getOffset());
			final int newEnd = Math.min(
					partition.getOffset() + partition.getLength(),
					region.getOffset() + region.getLength());
			return new Region(newOffset, newEnd - newOffset);
		}
		return region;
	}

 	private static int skipSpaces(final char[] line, int index, int offsetInLine) {
 		while (index < offsetInLine && Character.isWhitespace(line[index])) {
 			++index;
 		}
 		return index;
 	}
 
 	private List<ICompletionProposal> completionOnTag(
 			ContentAssistInvocationContext context, String tag) {
 		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		final JSDocTemplateCompletionProcessor processor = new JSDocTemplateCompletionProcessor(
 				(ScriptContentAssistInvocationContext) context);
 		Collections.addAll(proposals, processor.computeCompletionProposals(
 				context.getViewer(), context.getInvocationOffset()));
 		//
 		final Set<String> tags = new HashSet<String>();
 		Collections.addAll(tags, JSDocTag.getTags());
 		ISourceModule module = null; // TODO detect source module
 		Collections.addAll(
 				tags,
 				JSKeywordManager.getInstance().getKeywords(
 						JSKeywordCategory.JS_DOC_TAG, module));
 		// TODO option to ignore @returns ?
 		tags.remove(JSDocTag.RETURNS);
 		// collect used tags, to show keywords only for missing ones
 		final Set<String> usedTags = new HashSet<String>();
 		for (ICompletionProposal proposal : proposals) {
 			if (proposal instanceof ScriptTemplateProposal) {
 				usedTags.add(((ScriptTemplateProposal) proposal)
 						.getTemplateName());
 			}
 		}
 		for (String jsdocTag : tags) {
 			if (CharOperation.prefixEquals(tag, jsdocTag)
 					&& !usedTags.contains(jsdocTag)) {
 				proposals.add(new ScriptCompletionProposal(jsdocTag + ' ',
 						context.getInvocationOffset() - tag.length(), tag
 								.length(), DLTKPluginImages
 								.get(DLTKPluginImages.IMG_OBJS_JAVADOCTAG),
 						jsdocTag, 90, true));
 			}
 		}
 		return proposals;
 	}
 
 	/**
 	 * @param context
 	 * @param prefix
 	 * @return
 	 */
 	private List<ICompletionProposal> completionOnType(
 			ContentAssistInvocationContext context, String prefix) {
 		if (context instanceof ScriptContentAssistInvocationContext) {
 			final JSCompletionEngine engine = getCompletionEngine();
 			if (engine != null) {
 				final ISourceModule module = ((ScriptContentAssistInvocationContext) context)
 						.getSourceModule();
 				final JavaScriptCompletionProposalCollector collector = new JavaScriptCompletionProposalCollector(
 						module);
 				collector.setAttribute(TypeMode.JSDOC, Boolean.TRUE);
 				collector
 						.setInvocationContext((ScriptContentAssistInvocationContext) context);
 				engine.setRequestor(collector);
 				engine.completeTypes(module, TypeMode.JSDOC, prefix.trim(),
 						context.getInvocationOffset());
 				return Arrays.<ICompletionProposal> asList(collector
 						.getScriptCompletionProposals());
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	private JSCompletionEngine getCompletionEngine() {
 		final ICompletionEngine[] engines = DLTKLanguageManager
 				.getCompletionEngines(JavaScriptNature.NATURE_ID);
 		if (engines != null) {
 			for (ICompletionEngine engine : engines) {
 				if (engine instanceof JSCompletionEngine) {
 					return (JSCompletionEngine) engine;
 				}
 			}
 		}
 		return null;
 	}
 
 	public List<IContextInformation> computeContextInformation(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		return Collections.emptyList();
 	}
 
 	public String getErrorMessage() {
 		return null;
 	}
 
 }
