 package org.eclipse.emf.compare.diagram.tests.show;
 
 import static com.google.common.base.Predicates.and;
 import static com.google.common.base.Predicates.instanceOf;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertSame;
 import static junit.framework.Assert.assertTrue;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.attributeValueMatch;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.ofKind;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.EMFCompare;
 import org.eclipse.emf.compare.diagram.Show;
 import org.eclipse.emf.compare.diagram.tests.AbstractTest;
 import org.eclipse.emf.compare.diagram.tests.DiagramInputData;
 import org.eclipse.emf.compare.diagram.tests.show.data.ShowInputData;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.junit.Test;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 
 @SuppressWarnings("nls")
 public class ShowTest extends AbstractTest {
 
 	private ShowInputData input = new ShowInputData();
 
 	@Test
 	public void testA1UseCase() throws IOException {
 		final Resource left = input.getA1Left();
 		final Resource right = input.getA1Right();
 
 		final IComparisonScope scope = EMFCompare.createDefaultScope(left, right);
		final Comparison comparison = EMFCompare.builder().setPostProcessorRegistry(getPostProcessorRegistry()).build().compare(scope);
 		
 		final List<Diff> differences = comparison.getDifferences();
 
 		// We should have no less and no more than 2 differences
 		assertSame(Integer.valueOf(2), Integer.valueOf(differences.size()));
 		
 		Predicate<? super Diff> attributeChangeDescription = attributeValueMatch(NotationPackage.Literals.VIEW__VISIBLE.getName(), Boolean.TRUE, false);
 		Predicate<? super Diff> showDescription = and(instanceOf(Show.class), ofKind(DifferenceKind.CHANGE));
 		
 		assertSame(Integer.valueOf(1), count(differences, instanceOf(Show.class)));
 
 		final Diff attributeChange = Iterators.find(differences.iterator(), attributeChangeDescription);
 		final Diff show = Iterators.find(differences.iterator(), showDescription);
 		
 		assertNotNull(attributeChange);
 		assertNotNull(show);
 		
 		assertSame(Integer.valueOf(1), Integer.valueOf(show.getRefinedBy().size()));
 		assertTrue(show.getRefinedBy().contains(attributeChange));
 		
 	}
 
 	@Override
 	protected DiagramInputData getInput() {
 		return null;
 	}
 
 }
