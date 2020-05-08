 package nl.javadude.monopoly.domain;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import nl.javadude.monopoly.domain.squares.*;
 
 public class PlayerTest {
 
 	private Player player;
 
 	@Before
 	public void before() {
 		player = new Player(new Board(), "John");
 	}
 
 	@Test
 	public void shouldPayRent() {
 		Player ownerPlayer = mock(Player.class);
 		Realty realty = mock(Realty.class);
 		when(realty.isUnowned()).thenReturn(false);
 		when(realty.owner()).thenReturn(ownerPlayer);
 		when(realty.getRent()).thenReturn(1000L);
 
 		player.setCurrentPosition(realty);
 		player.payRent();
 
 		verify(ownerPlayer).receiveMoney(1000L);
 		assertThat(player.getMoney(), is(500L));
 	}
 
 @Test
    	public void payWhenMoneyEnough_paid() {
         player.setMoney(10);
         boolean paid = player.pay(9, Bank.BANK);
         assertTrue(paid);
     }
 
     @Test
    	public void payWhenMoneyNotEnough_notPaid() {
         player.setMoney(10);
         boolean paid = player.pay(11, Bank.BANK);
        assertFalse(paid);
     }
 
     @Test
     public void shouldDeductBalanceWhenBuying() {
         Realty realty = new Realty("Test", 1500, 0);
         player.setCurrentPosition(realty);
         assertThat(player.buy(), is(true));
         assertThat(player.getMoney(), is(0l));
     }
 
 
     @Test
    	public void shouldNotGoInDebtWhenBuying() {
             Realty realty = new Realty("Test", 15000, 0);
             player.setCurrentPosition(realty);
             assertThat(player.buy(), is(false));
             assertThat(player.getMoney(), is(1500l));
    	}
 
     @Test
     public void shouldNotGoInDebtWhenPaying() {
         MoneyExchanger receiver = mock(MoneyExchanger.class);
         player.pay(2000l, receiver);
         assertThat(player.getMoney(), is(0l));
         verify(receiver).receiveMoney(1500l);
     }
 }
