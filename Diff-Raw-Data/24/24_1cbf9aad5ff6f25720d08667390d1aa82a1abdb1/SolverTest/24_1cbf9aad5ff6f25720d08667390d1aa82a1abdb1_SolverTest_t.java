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
        ArrayFieldState fullField = new FieldStateFixtureBuilder()
                .row("2x1  1xx1   112x")
                .row("x21  2331   1x21")
                .row("11   1x11121211 ")
                .row("1211 1222x3x1   ")
                .row("x2x1123x33x21 11")
                .row("12111xx22x33222x")
                .row("    122112x2xx21")
                .row("      122212332 ")
                .row("    112xx1112x1 ")
                .row("    1x22211x2121")
                .row("  11211   111 1x")
                .row("112x31  111 1121")
                .row("x12xx2  1x211x21")
                .row("1113x21122x112x2")
                .row(" 123211x1111 13x")
                .row(" 1xx1 111     2x")
            .build();
 
        ArrayFieldState startField = new FieldPreopener().preopen(fullField);
 
         FieldApi fieldApi = new SimpleFieldApi(fullField, startField);
 
         Solver solver = new Solver(fieldApi);
         FieldState state = solver.solve();
 
         assertThat(state, Matchers.<FieldState>equalTo(fullField ));
     }
 }
