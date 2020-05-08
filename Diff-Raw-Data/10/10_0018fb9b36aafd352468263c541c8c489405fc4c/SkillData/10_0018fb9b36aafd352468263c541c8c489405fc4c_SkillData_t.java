 package com.pieno.jeu;
 
 import com.pieno.jeu.Server2D.Skill;
 
 public class SkillData {
 	public int[] hitTime = new int[6];
 	public float[] dmgMult = { 1, 1, 1, 1, 1, 1 };
 	public float[] KBSpeed = new float[6];
 	public long skillTime;
 	public int maxEnemiesHit, width = 120, height = 200;
 	public int manaUsed = 0, lvl;
 
 	public SkillData(int skill, int lvl){
 	switch (skill)
 	{
 	case Skill.ATTACK:
 		manaUsed = -5;
 		dmgMult[0] = 1;
 		maxEnemiesHit = 1;
 		KBSpeed[0] = 0.18f;
 		skillTime = 400;
 		hitTime[0] = 200;
 		width = 200;
 		break;
 	case Skill.Smash:
 		manaUsed = 15;
 		dmgMult[0] = 1.1f + 0.12f * lvl;
 		maxEnemiesHit = 3 + (int) (lvl * 0.4f);
 		KBSpeed[0] = 0.18f;
 		skillTime = 400;
 		hitTime[0] = 200;
 		width = 280;
 		break;
 	case Skill.MultiHit:
 		manaUsed = -2;
 		dmgMult[0] = dmgMult[1] = dmgMult[2] = 0.44f + 0.04f * lvl;
 		maxEnemiesHit = 1;
		KBSpeed[0] = KBSpeed[1] = 0.06f;
		KBSpeed[2] = 0.12f;
 		skillTime = 500;
 		hitTime[0] = 140;
 		hitTime[1] = 270;
 		hitTime[2] = 400;
 		width = 200;
 		break;
 	case Skill.Arrow:
 		manaUsed = 0;
 		dmgMult[0] = 1;
 		KBSpeed[0] = 0.10f;
 		maxEnemiesHit = 1;
 		hitTime[0] = 200;
 		skillTime = 400;
 		break;
 	case Skill.DoubleArrow:
 		manaUsed = 12;
 		dmgMult[0] = dmgMult[1] = 0.74f + 0.06f * lvl;
 		KBSpeed[0] = 0.05f;
 		KBSpeed[1] = 0.10f;
 		maxEnemiesHit = 1;
 		skillTime = 500;
 		hitTime[0] = 150;
 		hitTime[1] = 300;
 		break;
 	case Skill.ExplosiveArrow:
 		manaUsed = 15;
 		dmgMult[0] = 0.84f + 0.06f * lvl;
 		KBSpeed[0] = 0.17f;
 		maxEnemiesHit = 3 + (int) (lvl * 0.4f);
 		hitTime[0] = 200;
 		skillTime = 400;
 		break;
 	case Skill.EnergyBall:
 		manaUsed = 0;
 		dmgMult[0] = 1;
 		KBSpeed[0] = 0.10f;
 		maxEnemiesHit = 1;
 		hitTime[0] = 200;
 		skillTime = 400;
 		break;
 	case Skill.FireBall:
 		manaUsed = 8 + (int) (lvl * 0.4);
 		dmgMult[0] = 1.34f + 0.11f * lvl;
 		KBSpeed[0] = 0.13f;
 		maxEnemiesHit = 1;
 		hitTime[0] = 200;
 		skillTime = 400;
 		break;
 	case Skill.Explosion:
 		manaUsed = 10 + lvl;
 		dmgMult[0] = 0.83f + 0.07f * lvl;
 		KBSpeed[0] = 0.17f;
 		maxEnemiesHit = 3 + (int) (lvl * 0.5f);
 		hitTime[0] = 400;
 		skillTime = 600;
 		break;
 	}
 	}
 	
 	public int getMaxHits(){
 		for(int i = 0; i < hitTime.length; i++){
 			if(hitTime[i] == 0) return i;
 		}
 		return 0;
 	}
 }
