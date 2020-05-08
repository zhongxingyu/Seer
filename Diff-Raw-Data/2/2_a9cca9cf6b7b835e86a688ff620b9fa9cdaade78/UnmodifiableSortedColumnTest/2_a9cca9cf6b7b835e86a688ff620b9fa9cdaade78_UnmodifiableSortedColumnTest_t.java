 package pl.rtshadow.jtriss.column.unmodifiable;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static pl.rtshadow.jtriss.test.ColumnElementGenerator.element;
 import static pl.rtshadow.jtriss.test.CommonAssertions.assertTheSameCollection;
 import static pl.rtshadow.jtriss.test.TestObjects.generateSortedColumnFrom;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import pl.rtshadow.jtriss.column.SortedColumn;
 
 @RunWith(MockitoJUnitRunner.class)
public class UnmodifiableSortedColumnTest {
   SortedColumn<Integer> column = generateSortedColumnFrom(0, 1, 2, 3, 4);
 
   @Test
   public void hasAppropriateSize() {
     assertThat(column.getSize()).isEqualTo(5);
   }
 
   @Test
   public void containsElementsAtAppropriatePositions() {
     assertThat(column.contains(element(2).atPosition(2).get())).isTrue();
     assertThat(column.contains(element(4).atPosition(4).get())).isTrue();
 
     assertThat(column.contains(element(6).atPosition(6).get())).isFalse();
   }
 
   @Test
   public void createsValidNonEmptySubColumn() {
     SortedColumn<Integer> subColumn = column.getSubColumn(2, 5);
 
     assertTheSameCollection(
         subColumn.iterator(),
         generateSortedColumnFrom(2, 3, 4).iterator());
 
     assertThat(subColumn.contains(element(1).atPosition(1).get())).isFalse();
     assertThat(subColumn.contains(element(2).atPosition(2).get())).isTrue();
     assertThat(subColumn.contains(element(4).atPosition(4).get())).isTrue();
 
     assertThat(subColumn.getElementPositionedAt(4).getPositionInColumn()).isEqualTo(4);
   }
 
   @Test
   public void createsValidEmptySubColumn() {
     SortedColumn<Integer> subColumn = column.getSubColumn(5, 5);
 
     assertThat(subColumn).isEmpty();
 
     assertThat(subColumn.contains(element(1).atPosition(1).get())).isFalse();
     assertThat(subColumn.contains(element(4).atPosition(4).get())).isFalse();
   }
 
   @Test(expected = UnsupportedOperationException.class)
   public void cannotModifyColumnViaIterator() {
     column.iterator().remove();
   }
 
   @Test(expected = UnsupportedOperationException.class)
   public void cannotModifySubColumnViaIterator() {
     column.getSubColumn(1, 3).iterator().remove();
   }
 }
