 package no.niths.domain.battlestation;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 
 import no.niths.domain.battlestation.Console;
 import no.niths.domain.battlestation.Game;
 import no.niths.domain.battlestation.Loan;
 import no.niths.domain.school.Student;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ConsoleTest {
     private static final String NAME = "Super Mario";
 
     private static Validator validator;
 
     @BeforeClass
     public static void setUp() {
         ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         validator = factory.getValidator();
     }
 
     @Test
     public void testShouldGenerateNewConsole() {
         Console console = new Console();
         console.setName(NAME);
 
         assertThat(NAME, is(equalTo(console.getName())));
     }
 
     @Test
     public void testValidationOfCorrectConsoleValues() {
         Console console = new Console(NAME);
 
         Set<ConstraintViolation<Console>> constraintViolations = validator
                 .validate(console);
 
         assertThat(0, is(equalTo(constraintViolations.size())));
     }
 
     @Test
     public void testValidationOfIncorrectConsoleValues() {
        Console console = new Console("KM ");
 
         Set<ConstraintViolation<Console>> constraintViolations = validator
                 .validate(console);
 
         constraintViolations = validator.validate(console);
         assertThat(1, is(equalTo(constraintViolations.size())));
     }
 
     @Test
     public void testGettingGameFromConsole() {
         Game game = new Game();
 
         List<Game> games = new ArrayList<Game>();
         games.add(game);
 
         Console console = new Console();
         console.setGames(games);
 
         assertThat(game, is(equalTo(console.getGames().get(0))));
     }
 
     @Test
     public void testGettingLoanFromConsole() {
         Loan loan = new Loan();
 
         Console console = new Console();
         console.setLoan(loan);
 
         assertThat(loan, is(equalTo(console.getLoan())));
     }
 
     @Test
     public void testTwoEqualConsoles() {
         Console console = new Console(NAME);
 
         Console equalConsole = console;
 
         assertThat(true, is(equalTo(console.equals(equalConsole))));
     }
 
     @Test
     public void testTwoConsolesWhichIsNotEqual() {
         Console console = new Console(NAME);
         console.setId(1L);
 
         Console notEqualConsole = new Console("Halo");
         notEqualConsole.setId(2L);
 
         assertThat(false, is(equalTo(console.equals(notEqualConsole))));
     }
 
     @Test
     public void testEqualsBetweenNotEqualObjects() {
         Console console = new Console(NAME);
 
         Student student = new Student();
 
         assertThat(false, is(equalTo(console.equals(student))));
     }
 }
