 /**
  *
  *   Copyright Alexander Rau, Mike Groezinger, Retroduction.org - All rights reserved
  *
  *   This file is part of Carma. Carma is licensed under the OSL 3.0. The OSL 3.0 is
  *   available here: http://www.opensource.org/licenses/osl-3.0.php
  *
  */
 package com.retroduction.carma.transformer.asm.aor;
 
 import org.objectweb.asm.Opcodes;
 
 public class DMUL_2_DDIV_Transition extends AOR_Transition {
 
 	public DMUL_2_DDIV_Transition() {
 		super();
		this.sourceInstruction = Opcodes.DMUL;
		this.targetInstruction = Opcodes.DDIV;
 	}
 
 	public String getName() {
 		return "DADD_to_DSUB";
 	}
 }
