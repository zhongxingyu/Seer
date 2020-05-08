 package sstTest;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import sst.ShieldControl;
 
 public class ShieldTests {
 	@Test
 	public void TransferEnergyToShields() {
 		ShieldControl shield = new ShieldControl();
 		int energyToTransfer = 1000;
 		shield.transferEnergy(energyToTransfer);
 		Assert.assertEquals(energyToTransfer, shield.getEnergyLevel());
 	}
 	
 	@Test
 	public void TestInitialEnergyConstructor() {
 		int initialEnergy = 1000;
 		ShieldControl shield = new ShieldControl(initialEnergy);
 		Assert.assertEquals(initialEnergy, shield.getEnergyLevel());
 	}
 	
 	@Test
 	public void OverloadMaximumShieldEnergy() {
 		ShieldControl shield = new ShieldControl(ShieldControl._MAX_SHIELD_ENERGY);
 		shield.transferEnergy(1000);
 		Assert.assertEquals(ShieldControl._MAX_SHIELD_ENERGY, shield.getEnergyLevel());
 	}
 	
 	@Test
 	public void NoLessThanZeroShieldEnergy() {
 		ShieldControl shield = new ShieldControl(1000);
 		int deficit = shield.transferEnergy(-2000);
 		Assert.assertEquals(0, shield.getEnergyLevel());
 		Assert.assertEquals(-1000, deficit);
 	}
 	
 	@Test
 	public void FindHowMuchIsLeftOverFromOverloadMaximumShieldEnergy() {
 		ShieldControl shield = new ShieldControl(ShieldControl._MAX_SHIELD_ENERGY);
 		int transferAmount = 1000;
 		int remainder = shield.transferEnergy(transferAmount);
 		Assert.assertEquals(transferAmount, remainder);
 	}
 	
 	@Test
 	public void DamageShields() {
 		ShieldControl shield = new ShieldControl(1000);
 		int damage = 500;
 		shield.takeDamage(damage);
 		Assert.assertEquals(500, shield.getEnergyLevel());
 	}
 	
 	@Test
 	public void DamageToShip() {
 		ShieldControl shield = new ShieldControl(1000);
 		int damage = 2000;
 		int subsystemDamage = shield.takeDamage(damage);
 		Assert.assertEquals(1000, subsystemDamage);
 	}
 	
 	@Test
 	public void DamageShieldControlSubsystem() {
 		ShieldControl shield = new ShieldControl(0);
 		Assert.assertEquals(false, shield.isDamaged());
		shield.getDamage(500);
 		Assert.assertEquals(true,  shield.isDamaged());
 		Assert.assertEquals(10, shield.getTenthsOfRepairDays());
 	}
 	
 //	@Test
 //	public void PreventEnergyTransferWhenDamaged() {
 //		ShieldControl shield = new ShieldControl(0);
 //		shield.damage(500);
 //		shield.transferEnergy(1000);
 //		Assert.assertEquals(0, shield.getEnergyLevel());
 //	}
 	
 }
