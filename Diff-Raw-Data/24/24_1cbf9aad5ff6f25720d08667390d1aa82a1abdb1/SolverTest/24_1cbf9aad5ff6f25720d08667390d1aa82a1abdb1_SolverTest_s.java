 package ru.alepar.minesweeper;
 
 import org.hamcrest.Matchers;
 import org.junit.Test;
 import ru.alepar.minesweeper.core.SimpleFieldApi;
 import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
 import ru.alepar.minesweeper.fieldstate.FieldGenerator;
 import ru.alepar.minesweeper.fieldstate.FieldPreopener;
 import ru.alepar.minesweeper.model.FieldApi;
 import ru.alepar.minesweeper.model.FieldState;
 import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;
 
 import static org.junit.Assert.assertThat;
 
 public class SolverTest {
 
     @Test
     public void solvesCaseWhereFourIterationsNeededToSolve() throws Exception {
         ArrayFieldState full = new FieldStateFixtureBuilder()
                 .row("1x2")
                 .row("12x")
                 .row(" 11")
                 .build();
 
         ArrayFieldState start = new FieldStateFixtureBuilder()
                 .row("...")
                 .row("..x")
                 .row("..1")
                 .build();
 
         FieldApi fieldApi = new SimpleFieldApi(full, start);
 
         Solver solver = new Solver(fieldApi);
         FieldState state = solver.solve();
 
         assertThat(state, Matchers.<FieldState>equalTo(full));
     }
 
     @Test
     public void solvesRandomMinesweeperMediumPreopenedCase() throws Exception {
        ArrayFieldState fullField = new FieldGenerator(16, 16, 40).generate();
        ArrayFieldState startField = new FieldPreopener().preopen(fullField);
 
        System.out.println(fullField.toString());
        System.out.println(startField.toString());
 
         FieldApi fieldApi = new SimpleFieldApi(fullField, startField);
 
         Solver solver = new Solver(fieldApi);
         FieldState state = solver.solve();
 
         assertThat(state, Matchers.<FieldState>equalTo(fullField ));
     }
 }
