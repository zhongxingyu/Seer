 package com.svanberg.household.web.pages;
 
 import com.svanberg.household.domain.Expense;
 import com.svanberg.household.service.CategoryService;
 import com.svanberg.household.service.ExpenseService;
 import com.svanberg.household.web.test.SpringWicketTest;
 
 import org.apache.wicket.util.tester.FormTester;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.springframework.data.domain.Pageable;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import static com.svanberg.household.web.pages.ExpensePage.*;
 import static com.svanberg.household.web.test.Assert.assertStateless;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class ExpensePageTest extends SpringWicketTest
 {
     public static final Expense SOME_EXPENSE = new Expense(new Date(), "Some expense", 7);
     public static final Expense OTHER_EXPENSE = new Expense(new Date(), "Other expense", 32);
 
     @Mock ExpenseService expenseService;
     @Mock CategoryService categoryService;
 
     private ExpensePage page;
 
     @Before
     public void setUp() throws Exception {
         List<Expense> expenses = Arrays.asList(SOME_EXPENSE, OTHER_EXPENSE);
 
         when(expenseService.count()).thenReturn( (long) expenses.size());
         when(expenseService.list(any(Pageable.class))).thenReturn(expenses);
     }
 
     private void startPage() {
         page = tester().startPage(ExpensePage.class);
     }
 
     @Test
     public void renders() throws Exception {
         startPage();
 
         tester().assertRenderedPage(ExpensePage.class);
     }
 
     @Test
     public void stateless() throws Exception
     {
         startPage();
 
         assertStateless(page);
     }
 
     @Test
     public void pagination_invisible_if_no_expenses() throws Exception
     {
         when(expenseService.count()).thenReturn(0L);
 
         startPage();
 
        tester().assertInvisible(path(FORM, PAGINATION));
     }
 
     @Test
     public void pagination_invisible_if_only_one_page() throws Exception
     {
         when(expenseService.count()).thenReturn(ROWS_PER_PAGE);
         when(expenseService.list(any(Pageable.class))).thenReturn(new ArrayList<Expense>());
 
         startPage();
 
        tester().assertInvisible(path(FORM, PAGINATION));
     }
 
     @Test
     public void pagination_visible_if_more_than_one_page() throws Exception
     {
         when(expenseService.count()).thenReturn(ROWS_PER_PAGE + 1);
         when(expenseService.list(any(Pageable.class))).thenReturn(new ArrayList<Expense>());
 
         startPage();
 
        tester().assertVisible(path(FORM, PAGINATION));
     }
 
     @Test
     public void delete() throws Exception
     {
         startPage();
 
         FormTester formTester = tester().newFormTester(FORM);
         markRow(formTester, 1, true);
         markRow(formTester, 2, false);
         formTester.submit(page.get(DELETE));
 
         ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
         verify(expenseService, times(1)).delete(captor.capture());
 
         assertEquals(SOME_EXPENSE, captor.getValue());
     }
 
     private void markRow(FormTester formTester, int row, boolean selected)
     {
         formTester.setValue(path(TABLE, "body", "rows", row, "cells", 1, "cell", "checkbox"), selected);
     }
 }
