 package circuitos;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import instrucoes.Instrucao;
 
 import mips.ALU;
 import mips.Controle;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import registradores.Reg;
 
 
 public class EXCircuitTest {
 	
 		private EXCircuit circuit;
 		private Controle controle;
 
 		@Before
 		public void setUp() {
 			this.circuit = new EXCircuit();
 			
 			preencheInputBusAddi(this.circuit);
 			this.controle = criaControleAddi();
 			this.circuit.setControl(this.controle);
 		}
 	
 
 		@Test
 		public void deveSaberReceberValoresDeRegistradoresNoBusDeEntrada() {
 			assertNotNull(this.circuit.getFromInputBus("rs"));
 			
 		}
 		
 		@Test
 		public void deveSaberReceberDadoImediatoNoBusDeEntrada() {
 			assertNotNull(this.circuit.getFromInputBus("imm"));
 			
 		}
 		
 		@Test
 		public void deveSaberEscolherOsInputsDaULACorretamente() {
 			assertEquals(Integer.valueOf(0), this.circuit.getALUInput1());	//R0 contem 0
 			assertEquals(Integer.valueOf(100), this.circuit.getALUInput2()); //usa 100 e nao R10
 		}
 		
 		@Test
 		public void deveSaberExecutarAddi() {
 			this.circuit.run();
 			assertEquals(Integer.valueOf(100), this.circuit.getFromOutputBus("result"));
 		}
 		
 		@Test
 		public void deveSaberExecutarAdd() {
 			circuit.putInInputBus("rs", 0);
 			circuit.putInInputBus("rt", 50);
 			circuit.setControl(criaControleAdd());
 			circuit.run();
 			
 			assertEquals(Integer.valueOf(50), this.circuit.getFromOutputBus("result"));
 		}
 		
 		@Test
 		public void deveSaberExecutarLw() {
 			//lw R6,28(R0)
 			circuit.putInInputBus("rs", 0);
 			circuit.putInInputBus("imm", 28);
 			circuit.setControl(criaControleLw());
 			circuit.run();
 			
 			assertEquals(Integer.valueOf(28), this.circuit.getFromOutputBus("result"));
 		}
 		
 		@Test
 		public void deveSaberExecutarSw() {
 			//sw R9,24(R0)
 			circuit.putInInputBus("rs", 0);
 			circuit.putInInputBus("imm", 24);
 			circuit.putInInputBus("rt", 10);  //R9 tem 10
 			circuit.setControl(criaControleSw());
 			circuit.run();
 			
 			assertEquals(Integer.valueOf(24), this.circuit.getFromOutputBus("result"));
 			assertEquals(Integer.valueOf(10), this.circuit.getFromOutputBus("rt"));
 		}
 		
 		
 		@Test
 		public void deveSaberExecutarBranchesEAtualizarControleCorretamente() {
 			//ble R1, R2, 120
 			circuit.putInInputBus("rs", 10);
 			circuit.putInInputBus("rt", 50);
 			circuit.putInInputBus("imm", 120);
 			circuit.putInInputBus("pc", 4);
 			Controle controle = criaControleBranch();
 			circuit.setControl(controle);
 			circuit.run();
 			
 			assertEquals(Integer.valueOf(-40), this.circuit.getFromOutputBus("result"));
			assertEquals(Integer.valueOf(4 + 4 * 120), this.circuit.getFromOutputBus("newpc"));
 			assertEquals(Integer.valueOf(1), controle.get("PCSrc"));
 		}
 		
 		
 		@Test
 		public void deveSaberExecutarNop() {
 			circuit.setControl(criaControleNop());
 			circuit.run();
 			
 			assertNull(this.circuit.getFromOutputBus("result"));
 		}
 
 		@Test
 		public void deveSaberExecutarMulEmDoisClocks() {
 			circuit.putInInputBus("rs", 10);
 			circuit.putInInputBus("rt", 50);
 			circuit.setControl(criaControleMul());
 
 			circuit.run(); //primeiro clock
 			assertTrue(this.circuit.isWorking());
 			assertNull(this.circuit.getFromOutputBus("result"));
 			
 			circuit.run(); //segundo clock
 			assertFalse(this.circuit.isWorking());
 			assertEquals(Integer.valueOf(500), this.circuit.getFromOutputBus("result"));
 		}
 		
 		
 		
 		/*
 		 * Apenas algumas fabrica de sinais de controle para ajudar nos testes
 		 */
 		private Controle criaControleAddi() {
 			//addi R10,R0,100
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.ADD_CODE);  //vai ser um add
 			controle.put("ALUSrc", 1); //vai usar dado immediato na ULA
 			controle.put("RegWr", 1);  // vai escrever em registrador
 			controle.put("RegWrID", 10);  // vai escrever em R10
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		
 		private Controle criaControleAdd() {
 			//add R10, R0, R1
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.ADD_CODE);  //vai ser um add
 			controle.put("ALUSrc", 0); //nao vai usar dado immediato na ULA
 			controle.put("RegWr", 1);  // vai escrever em registrador
 			controle.put("RegWrID", 10);  // vai escrever em R10
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		private Controle criaControleBranch() {
 			//ble R1, R2, 120
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.SUB_CODE);  //vai ser um sub
 			controle.put("ALUSrc", 0); //nao vai usar dado immediato na ULA
 			controle.put("RegWr", 0);  // nao vai escrever em registrador
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 1);  //eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			controle.put("BranchType", "ble");  //eh ble
 			return controle;
 		}
 		private Controle criaControleNop() {
 			//nop
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.NOP_CODE);  //vai ser um nop
 			controle.put("ALUSrc", 0); //nao vai usar dado immediato na ULA
 			controle.put("RegWr", 0);  // nao vai escrever em registrador
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		private Controle criaControleMul() {
 			//mul R7,R6,R5
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.MUL_CODE);  //vai ser um mul
 			controle.put("ALUSrc", 0); //nao vai usar dado immediato na ULA
 			controle.put("RegWr", 1);  // vai escrever em registrador
 			controle.put("RegWrID", 7);  // vai escrever em R7
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		private Controle criaControleLw() {
 			//lw R6,28(R0)
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.ADD_CODE);  //vai ser um add
 			controle.put("ALUSrc", 1); //vai usar dado immediato na ULA
 			controle.put("RegWr", 1);  // vai escrever em registrador
 			controle.put("RegWrID", 6);  // vai escrever em R10
 			controle.put("MemToReg", 1);  //vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 0);  //nao vai escrever na memoria
 			controle.put("MemRead", 1);  //vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		
 		private Controle criaControleSw() {
 			//sw R9,24(R0)
 			Controle controle = new Controle();
 			controle.put("ALUOp", ALU.ADD_CODE);  //vai ser um add
 			controle.put("ALUSrc", 1); //vai usar dado immediato na ULA
 			controle.put("RegWr", 0);
 			controle.put("MemToReg", 0);  //nao vai ser uma escrita de memoria para registrador 
 			controle.put("MemWr", 1);  //vai escrever na memoria
 			controle.put("MemRead", 0);  //nao vai ler da memoria
 			controle.put("Branch", 0);  //nao eh branch
 			controle.put("Jump", 0);  //nao eh jump
 			return controle;
 		}
 		
 		private void preencheInputBusAddi(EXCircuit circuit) {
 			//addi R10,R0,100
 			circuit.putInInputBus("rs", 0);
 			circuit.putInInputBus("imm", 100);
 		}
 
 
 }
