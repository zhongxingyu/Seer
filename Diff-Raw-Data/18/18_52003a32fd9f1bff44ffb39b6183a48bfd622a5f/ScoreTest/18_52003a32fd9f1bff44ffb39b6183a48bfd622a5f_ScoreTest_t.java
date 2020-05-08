 package pl.edu.agh.to1.dice.logic.players;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import pl.edu.agh.to1.dice.logic.figures.Bonus;
 import pl.edu.agh.to1.dice.logic.figures.Figure;
 import pl.edu.agh.to1.dice.logic.figures.FigureManager;
 import pl.edu.agh.to1.dice.logic.figures.IFigure;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Michal Partyka
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ScoreTest {
     @Mock
     private FigureManager figureManager;
     @InjectMocks
    private Score score = new Score();
 
     @Test
     public void testGetResult() throws Exception {
         List<Bonus> bonuses = Arrays.asList(new Bonus(Arrays.asList((IFigure) Figure.ONES), 10, 20, "bonus"));
         when(figureManager.getBonuses()).thenReturn(bonuses);
         score.initialize();
         assertThat(score.getResult().getBonuses()).isEqualTo(bonuses);
     }
 }
