 package protobuf.structureView;
 
import org.jetbrains.annotations.NotNull;
 import com.intellij.ide.structureView.StructureViewBuilder;
 import com.intellij.ide.structureView.StructureViewModel;
 import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
 import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
 import com.intellij.psi.PsiFile;
 
 /**
  * @author Nikolay Matveev
  */
public class PbStructureViewBuilderProvider implements PsiStructureViewFactory
{
	@Override
	public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile)
	{
		return new TreeBasedStructureViewBuilder()
		{
			@NotNull
			@Override
			public StructureViewModel createStructureViewModel(Editor editor)
			{
				return new PbStructureViewModel(psiFile);
			}
		};
	}
 }
