 package fdk.proto;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 
 public class Prototype {
 
     // GENERAL
     public static final int PROTO_ID       = 0x00;
     public static final int DESCR_ID       = 0x04;
     public static final int FID            = 0x08;
 
     // ITEM
     public static final int LIGHT_DIST     = 0x0c;
     public static final int LIGHT_INTENS   = 0x10;
 
     public static final int FLAGS          = 0x14;
     public static final int FLAGS_EXT      = 0x18;
 
     public static final int SCRIPT         = 0x1c;
     public static final int SUB_TYPE       = 0x20;
     public static final int MATERIAL       = 0x24;
     public static final int SIZE           = 0x28;
     public static final int WEIGHT         = 0x2c;
     public static final int COST           = 0x30;
     public static final int INV_FID        = 0x34;
     public static final int SOUND          = 0x38;
 
     // ARMOR
     public static final int AC             = 0x39;
 
     public static final int DR_NORMAL      = 0x3d;
     public static final int DR_LASER       = 0x41;
     public static final int DR_FIRE        = 0x45;
     public static final int DR_PLASMA      = 0x49;
     public static final int DR_ELEC        = 0x4d;
     public static final int DR_EMP         = 0x51;
     public static final int DR_EXPL        = 0x55;
 
     public static final int DT_NORMAL      = 0x59;
     public static final int DT_LASER       = 0x5d;
     public static final int DT_FIRE        = 0x61;
     public static final int DT_PLASMA      = 0x65;
     public static final int DT_ELEC        = 0x69;
     public static final int DT_EMP         = 0x6d;
     public static final int DT_EXPL        = 0x71;
 
     public static final int ARMOR_PERK     = 0x75;
     public static final int MALE_FID       = 0x79;
     public static final int FEMALE_FID     = 0x7d;
 
     // CONTAINER
     public static final int MAX_SIZE       = 0x39;
     public static final int OPEN_FLAGS     = 0x3d;
 
     // DRUG
     public static final int STAT_0         = 0x39;
     public static final int STAT_1         = 0x3d;
     public static final int STAT_2         = 0x41;
 
     public static final int AMOUNT00       = 0x45;
     public static final int AMOUNT01       = 0x49;
     public static final int AMOUNT02       = 0x4d;
 
     public static final int DURATION1      = 0x51;
     public static final int AMOUNT10       = 0x55;
     public static final int AMOUNT11       = 0x59;
     public static final int AMOUNT12       = 0x5d;
 
     public static final int DURATION2      = 0x61;
     public static final int AMOUNT20       = 0x65;
     public static final int AMOUNT21       = 0x69;
     public static final int AMOUNT22       = 0x6d;
 
     public static final int ADDICT_RATE    = 0x71;
     public static final int W_EFFECT       = 0x75;
     public static final int W_ONSET        = 0x79;
 
     // WEAPON
     public static final int ANIM_CODE      = 0x39;
     public static final int MIN_DMG        = 0x3d;
     public static final int MAX_DMG        = 0x41;
     public static final int DMG_TYPE       = 0x45;
     public static final int MAX_RANGE1     = 0x49;
     public static final int MAX_RANGE2     = 0x4d;
     public static final int PROJ_PID       = 0x51;
     public static final int MIN_ST         = 0x55;
     public static final int AP_COST1       = 0x59;
     public static final int AP_COST2       = 0x5d;
     public static final int CRIT_FAIL      = 0x61;
     public static final int WEAPON_PERK    = 0x65;
     public static final int ROUNDS         = 0x69;
     public static final int WEAPON_CALIBER = 0x6d;
     public static final int AMMO_PID       = 0x71;
     public static final int MAX_AMMO       = 0x75;
     public static final int WEAPON_SOUND   = 0x79;
 
     // AMMO
     public static final int AMMO_CALIBER   = 0x39;
     public static final int QUANTITY       = 0x3d;
     public static final int AC_AJUST       = 0x41;
     public static final int DR_ADJUST      = 0x45;
     public static final int DMG_MULT       = 0x49;
     public static final int DMG_DIV        = 0x4d;
 
     // MISK
     public static final int POWER_PID      = 0x39;
     public static final int POWER_TYPE     = 0x3d;
     public static final int CHARGES        = 0x41;
 
     // KEY
     public static final int KEY_UNK        = 0x39;
 
     // CRITTERS
     public static final int HEAD_FID       = 0x20;
     public static final int AI_PACKET      = 0x24;
     public static final int TEAM_NUM       = 0x28;
     public static final int CRITTER_FLAGS  = 0x2c;
 
     public static final int STR            = 0x30;
     public static final int PER            = 0x34;
     public static final int END            = 0x38;
     public static final int CHR            = 0x3c;
     public static final int INT            = 0x40;
     public static final int AGL            = 0x44;
     public static final int LCK            = 0x48;
 
     public static final int HP             = 0x4c;
     public static final int AP             = 0x50;
     public static final int CRITTER_AC     = 0x54;
     public static final int UNARMED_DMG    = 0x58;
     public static final int MELEE_DMG      = 0x5c;
     public static final int CARRY_WEIGHT   = 0x60;
     public static final int SEQUENCE       = 0x64;
     public static final int HEALING_RATE   = 0x68;
     public static final int CRIT_CHANCE    = 0x6c;
     public static final int BETTER_CRIT    = 0x70;
 
     public static final int C_DT_NORMAL    = 0x74;
     public static final int C_DT_LASER     = 0x78;
     public static final int C_DT_FIRE      = 0x7c;
     public static final int C_DT_PLASMA    = 0x80;
     public static final int C_DT_ELEC      = 0x84;
     public static final int C_DT_EMP       = 0x88;
     public static final int C_DT_EXPL      = 0x8c;
     public static final int C_DR_NORMAL    = 0x90;
     public static final int C_DR_LASER     = 0x94;
     public static final int C_DR_FIRE      = 0x98;
     public static final int C_DR_PLASMA    = 0x9c;
     public static final int C_DR_ELEC      = 0xa0;
     public static final int C_DR_EMP       = 0xa4;
     public static final int C_DR_EXPL      = 0xa8;
     public static final int C_DR_RAD       = 0xac;
     public static final int C_DR_POIS      = 0xb0;
 
     public static final int AGE            = 0xb4;
     public static final int GENDER         = 0xb8;
 
     public static final int STR_P          = 0xbc;
     public static final int PER_P          = 0xc0;
     public static final int END_P          = 0xc4;
     public static final int CHR_P          = 0xc8;
     public static final int INT_P          = 0xcc;
     public static final int AGL_P          = 0xd0;
     public static final int LCK_P          = 0xd4;
 
     public static final int HP_P           = 0xd8;
     public static final int AP_P           = 0xdc;
     public static final int CRITTER_AC_P   = 0xe0;
     public static final int UNARMED_P      = 0xe4;
     public static final int MELEE_P        = 0xe8;
     public static final int CARRY_WEIGHT_P = 0xec;
     public static final int SEQUENCE_P     = 0xf0;
     public static final int HEALING_RATE_P = 0xf4;
     public static final int CRIT_CHANCE_P  = 0xf8;
     public static final int BETTER_CRIT_P  = 0xfc;
 
     public static final int C_DT_NORMAL_P  = 0x100;
     public static final int C_DT_LASER_P   = 0x104;
     public static final int C_DT_FIRE_P    = 0x108;
     public static final int C_DT_PLASMA_P  = 0x10c;
     public static final int C_DT_ELEC_P    = 0x110;
     public static final int C_DT_EMP_P     = 0x114;
     public static final int C_DT_EXPL_P    = 0x118;
     public static final int C_DR_NORMAL_P  = 0x11c;
     public static final int C_DR_LASER_P   = 0x120;
     public static final int C_DR_FIRE_P    = 0x124;
     public static final int C_DR_PLASMA_P  = 0x128;
     public static final int C_DR_ELEC_P    = 0x12c;
     public static final int C_DR_EMP_P     = 0x130;
     public static final int C_DR_EXPL_P    = 0x134;
     public static final int C_DR_RAD_P     = 0x138;
     public static final int C_DR_POIS_P    = 0x13c;
 
     public static final int AGE_P          = 0x140;
     public static final int GENDER_P       = 0x144;
 
     public static final int SMALL_GUNS     = 0x148;
     public static final int BIG_GUNS       = 0x14c;
     public static final int ENERG_WEP      = 0x150;
     public static final int UNARMED        = 0x154;
     public static final int MELEE          = 0x158;
     public static final int THROWING       = 0x15c;
     public static final int FIRST_AID      = 0x160;
     public static final int DOCTOR         = 0x164;
     public static final int SNEAK          = 0x168;
     public static final int LOCKPIK       = 0x16c;
     public static final int STEAL          = 0x170;
     public static final int TRAPS          = 0x174;
     public static final int SCIENCE        = 0x178;
     public static final int REPAIR         = 0x17c;
     public static final int SPEECH         = 0x180;
     public static final int BARTER         = 0x184;
     public static final int GAMBLING       = 0x188;
     public static final int OUTDOORSMAN    = 0x18c;
 
     public static final int BODY_TYPE      = 0x190;
     public static final int EXP            = 0x194;
     public static final int KILL_TYPE      = 0x198;
     public static final int C_DMG_TYPE     = 0x19c;
 
     // SCENERY
     public static final int SCEN_SOUND     = 0x28;
 
     // PORTAL
     public static final int WALK_THRU      = 0x29;
     public static final int PORTAL_UNK     = 0x2d;
 
     // STAIRS
     public static final int DST_POS        = 0x29;
     public static final int DST_MAP        = 0x2d;
 
     // ELEVATOR
     public static final int ELEV_TYPE      = 0x29;
     public static final int ELEV_LEVEL     = 0x2d;
 
     // LADDER BOTTOM
     public static final int LB_DST_POS     = 0x29;
 
     // LADDER TOP
     public static final int LT_DST_POS     = 0x29;
 
     // GENERIC SCENERY
     public static final int GS_UNK         = 0x29;
 
     private ByteBuffer      m_buffer;
 
     public Prototype(InputStream in) throws IOException {
         byte[] buf = new byte[512];
         int len = 0;
         int off = 0;
        while ((len = in.read(buf, off, 512 - off)) != -1)
             off += len;
 
         m_buffer = ByteBuffer.wrap(buf, 0, off);
     }
 
     public int get(int field) {
         return m_buffer.getInt(field);
     }
 
     public int getSound(int field) {
         return m_buffer.get(field);
     }
 
     public void set(int field, int val) {
         m_buffer.putInt(field, val);
     }
 
     public void setSound(int field, byte val) {
         m_buffer.put(field, val);
     }
 
     public void write(OutputStream os) throws IOException {
         os.write(m_buffer.array());
     }
 
     public int getType() {
         return (get(PROTO_ID) >> 24) & 0x0f;
     }
 
 }
