 package org.napile.idea.plugin.editor.lineMarker;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.lang.psi.NapileClassBody;
import org.napile.compiler.lang.psi.NapileConstructor;
import org.napile.compiler.lang.psi.NapileNamedMethodOrMacro;
 import com.intellij.codeHighlighting.Pass;
 import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
 import com.intellij.codeInsight.daemon.LineMarkerInfo;
 import com.intellij.codeInsight.daemon.LineMarkerProvider;
 import com.intellij.openapi.editor.colors.CodeInsightColors;
 import com.intellij.openapi.editor.colors.EditorColorsManager;
 import com.intellij.openapi.editor.colors.EditorColorsScheme;
 import com.intellij.openapi.editor.markup.GutterIconRenderer;
 import com.intellij.openapi.editor.markup.SeparatorPlacement;
 import com.intellij.psi.PsiElement;
 import com.intellij.util.FunctionUtil;
 
 /**
  * @author VISTALL
  * @since 19:11/20.02.13
  */
 public class MethodSeparatorLineMarkerProvider implements LineMarkerProvider
 {
 	protected final DaemonCodeAnalyzerSettings daemonCodeAnalyzerSettings;
 	protected final EditorColorsManager editorColorsManager;
 
 	public MethodSeparatorLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager)
 	{
 		daemonCodeAnalyzerSettings = daemonSettings;
 		editorColorsManager = colorsManager;
 	}
 
 	@Nullable
 	@Override
 	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
 	{
		if(daemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS && (element instanceof NapileNamedMethodOrMacro || element instanceof NapileConstructor))
 		{
 			if(element.getNode().getTreeParent() == null)
 				return null;
 
			NapileClassBody classBody = (NapileClassBody) element.getParent();
			if(classBody.getDeclarations()[0] == element)
 				return null;
 
 			LineMarkerInfo info = new LineMarkerInfo<PsiElement>(element, element.getTextRange(), null, Pass.UPDATE_ALL, FunctionUtil.<Object, String>nullConstant(), null, GutterIconRenderer.Alignment.RIGHT);
 			EditorColorsScheme scheme = editorColorsManager.getGlobalScheme();
 			info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
 			info.separatorPlacement = SeparatorPlacement.TOP;
 			return info;
 		}
 
 		return null;
 	}
 
 	@Override
 	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result)
 	{
 
 	}
 }
