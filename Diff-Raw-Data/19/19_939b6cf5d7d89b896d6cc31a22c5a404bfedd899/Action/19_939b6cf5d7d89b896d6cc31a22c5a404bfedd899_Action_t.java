 // **********************************************************************
 //
 // Copyright (c) 2003-2009 ZeroC, Inc. All rights reserved.
 //
 // This copy of Ice is licensed to you under the terms described in the
 // ICE_LICENSE file included in this distribution.
 //
 // **********************************************************************
 
 // Ice version 3.3.1
 
 package NWN;
 
 public enum Action implements java.io.Serializable
 {
     MoveToPointAction,
     PickupItemAction,
     DropItemAction,
     AttackObjectAction,
     CastSpellAction,
     OpenDoorAction,
     CloseDoorAction,
     DialogObjectAction,
     DisableTrapAction,
     RecoverTrapAction,
     FlagTrapAction,
     ExamineTrapAction,
     SetTrapAction,
     OpenLockAction,
     LockAction,
     UseObjectAction,
     AnimalEmpathyAction,
     RestAction,
     TauntAction,
     ItemCastSpellAction,
     CounterSpellAction,
     HealAction,
     PickpocketAction,
     FollowAction,
    WaitAction,
     SitAction,
     SmiteGoodAction,
     KIDamageAction,
     RandomWalkAction,
     InvalidAction;
 
     public static final int _MoveToPointAction = 0;
     public static final int _PickupItemAction = 1;
     public static final int _DropItemAction = 2;
     public static final int _AttackObjectAction = 3;
     public static final int _CastSpellAction = 4;
     public static final int _OpenDoorAction = 5;
     public static final int _CloseDoorAction = 6;
     public static final int _DialogObjectAction = 7;
     public static final int _DisableTrapAction = 8;
     public static final int _RecoverTrapAction = 9;
     public static final int _FlagTrapAction = 10;
     public static final int _ExamineTrapAction = 11;
     public static final int _SetTrapAction = 12;
     public static final int _OpenLockAction = 13;
     public static final int _LockAction = 14;
     public static final int _UseObjectAction = 15;
     public static final int _AnimalEmpathyAction = 16;
     public static final int _RestAction = 17;
     public static final int _TauntAction = 18;
     public static final int _ItemCastSpellAction = 19;
     public static final int _CounterSpellAction = 20;
     public static final int _HealAction = 21;
     public static final int _PickpocketAction = 22;
     public static final int _FollowAction = 23;
    public static final int _WaitAction = 24;
    public static final int _SitAction = 25;
    public static final int _SmiteGoodAction = 26;
    public static final int _KIDamageAction = 27;
    public static final int _RandomWalkAction = 28;
    public static final int _InvalidAction = 29;
 
     public static Action
     convert(int val)
     {
        assert val >= 0 && val < 30;
         return values()[val];
     }
 
     public static Action
     convert(String val)
     {
         try
         {
             return valueOf(val);
         }
         catch(java.lang.IllegalArgumentException ex)
         {
             return null;
         }
     }
 
     public int
     value()
     {
         return ordinal();
     }
 
     public void
     __write(IceInternal.BasicStream __os)
     {
         __os.writeByte((byte)value());
     }
 
     public static Action
     __read(IceInternal.BasicStream __is)
     {
        int __v = __is.readByte(30);
         return Action.convert(__v);
     }
 }
