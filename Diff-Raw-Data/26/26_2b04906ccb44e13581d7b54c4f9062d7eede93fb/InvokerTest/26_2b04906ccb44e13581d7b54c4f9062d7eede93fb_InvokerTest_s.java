 package Command;
 
 import Player.Player;
 import Player.Position;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class InvokerTest {
     @Test
     public void it_should_execute_roll_command_when_input_command_is_roll() {
         //given
         Player player = new Player(new Position(0));
         Invoker invoker = new Invoker();
         //when
         invoker.setCommand("roll");
         invoker.execute(player);
         //then
        assertThat(player, is(new Player(new Position(5))));
     }
 }
