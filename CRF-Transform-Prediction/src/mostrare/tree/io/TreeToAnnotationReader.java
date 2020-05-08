package mostrare.tree.io;

import java.io.File;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.impl.TreeAST;

public interface TreeToAnnotationReader {

	public abstract TreeAST readTree(File file, CRFWithConstraintNode crf);
}
