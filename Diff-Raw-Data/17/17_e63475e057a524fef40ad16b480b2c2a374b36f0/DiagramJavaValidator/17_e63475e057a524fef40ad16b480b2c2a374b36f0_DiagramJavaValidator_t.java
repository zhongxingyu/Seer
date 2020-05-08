 package org.facttype.validation;
 
 import org.eclipse.xtext.validation.Check;
 import org.facttype.diagram.Column;
 import org.facttype.diagram.DiagramPackage;
 import org.facttype.diagram.FactTypeDiagram;
import org.facttype.diagram.PrimaryKey;
 
 /**
  * Fact Type Diagram model checkers. These model checks are used to give
  * feedback to invalid models while you edit your fact type diagram model in the
  * IDE model editor.
  */
 public class DiagramJavaValidator extends AbstractDiagramJavaValidator {
 
 	/**
 	 * Checks if the column name starts with a capital. This is mainly here
 	 * because it was the xtext Check demo, but it also seems to be suitable.
 	 * 
 	 * @param column
 	 */
 	@Check
 	public void checkColumnStartsWithCapital(Column column) {
 		if (!Character.isUpperCase(column.getName().charAt(0))) {
 			warning("Name should start with a capital",
 					DiagramPackage.Literals.COLUMN__NAME);
 		}
 	}
 
 	/**
 	 * Checks if there is a column defined with a different number of example
 	 * values. All columns should have the same number of values.
 	 * 
 	 * @param diagram
 	 *            The diagram to validate.
 	 */
 	@Check
 	public void checkColumnHaveSameNumberOfValues(FactTypeDiagram diagram) {
 		Column previousColumn = null;
 		int i = 0;
 		for (Column column : diagram.getColumns()) {
 			if (previousColumn != null) {
 				if (previousColumn.getValues().size() != column.getValues()
 						.size()) {
 					error(previousColumn.getName() + ".values size "
 							+ previousColumn.getValues().size()
 							+ " is not equal to " + column.getName()
 							+ ".values size " + column.getValues().size(),
 							DiagramPackage.Literals.FACT_TYPE_DIAGRAM__COLUMNS,
 							i);
 				}
 			}
 			previousColumn = column;
 			i++;
 		}
 	}
 
	/**
	 * Checks if the primaryKey columns do not have notEmpty defined, because a 
	 * primary key is automatically "not empty".
	 * 
	 * @param primaryKey
	 */
	@Check
	public void checkPrimaryKeyColumnsNotEmpty(PrimaryKey primaryKey) {
		for (Column column : primaryKey.getColumns()) {
			if (column.isNotEmpty()) {
				error("Part of PrimaryKey, which implies notEmpty. No need to set notEmpy.",
						column, DiagramPackage.Literals.COLUMN__NOT_EMPTY, -1);
			}
		}
	}
 }
