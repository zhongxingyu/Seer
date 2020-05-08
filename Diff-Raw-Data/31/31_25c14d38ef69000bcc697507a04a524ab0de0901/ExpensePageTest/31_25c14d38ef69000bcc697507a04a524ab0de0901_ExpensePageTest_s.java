 package com.svanberg.household.web.expense;
 
 import com.svanberg.household.domain.Expense;
 import com.svanberg.household.service.CategoryService;
 import com.svanberg.household.service.ExpenseService;
 import com.svanberg.household.web.test.SpringWicketTest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.data.domain.Pageable;
 
 import java.util.ArrayList;
 
 import static com.svanberg.household.web.expense.ExpensePage.*;
 import static com.svanberg.household.web.test.Assert.assertStateless;
import static org.junit.Assert.assertNotNull;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class ExpensePageTest extends SpringWicketTest
 {

     @Mock ExpenseService expenseService;
     @Mock CategoryService categoryService;
 
     private ExpensePage page;
 
     @Before
     public void setUp() throws Exception {
         page = tester().startPage(ExpensePage.class);
     }
 
     @Test
     public void renders() throws Exception {
        assertNotNull("Page does not render", page);
     }
 
     @Test
     public void stateless() throws Exception
     {
         assertStateless(page);
     }
 
     @Test
     public void pagination_invisible_if_no_expenses() throws Exception
     {
         when(expenseService.count()).thenReturn(0L);
 
        setUp();
 
         tester().assertInvisible(PAGINATION);
     }
 
     @Test
     public void pagination_invisible_if_only_one_page() throws Exception
     {
         when(expenseService.count()).thenReturn(ROWS_PER_PAGE);
         when(expenseService.list(any(Pageable.class))).thenReturn(new ArrayList<Expense>());
 
        setUp();
 
         tester().assertInvisible(PAGINATION);
     }
 
     @Test
     public void pagination_visible_if_more_than_one_page() throws Exception
     {
         when(expenseService.count()).thenReturn(ROWS_PER_PAGE + 1);
         when(expenseService.list(any(Pageable.class))).thenReturn(new ArrayList<Expense>());
 
        setUp();
 
         tester().assertVisible(PAGINATION);
     }
 }
