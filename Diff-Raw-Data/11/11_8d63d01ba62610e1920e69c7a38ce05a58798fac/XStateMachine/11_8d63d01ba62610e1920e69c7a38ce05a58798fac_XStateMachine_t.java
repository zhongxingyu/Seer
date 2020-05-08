 package com.xengine.android.toolkit.sm;
 
 import java.util.List;
 
 /**
  * 通用状态机的接口。
  * Created by jasontujun on 2015/3/21.
  */
 public interface XStateMachine {
 
     /**
      * 状态机的针对状态变化的监听接口
      */
     interface Listener {
 
         /**
          * 当状态机发生状态转变时，会回调此方法。
          * @param state 状态机转变后最新的状态
         * @param action 导致这个状态发生的动作
          * @param sm 状态机对象实例
          */
        void onState(String state, XAction action, XStateMachine sm);
     }
 
     /**
      * 初始化状态机。
      * @param startState 起始状态
      * @param endState 终止状态
      * @param states 状态机中所有的状态
      */
 	boolean init(String startState, String endState, String[] states);
 
     /**
      * 启动状态机。
      */
 	void start();
 
     /**
      * 暂停状态机。
      */
 	void pause();
 
     /**
      * 终止状态机。停止并将状态设置成结束状态。
      */
     void end();
 
     /**
      * 重启状态机。停止并将状态设置成初始状态
      */
     void reset();
 
     /**
      * 触发单个动作，从而尝试让状态机进入下一个状态。
      * @param action
      */
 	boolean act(XAction action);
 
     /**
      * 触发一系列动作，从而尝试让状态机进入下一个状态。
      * @param actions 动作链
      */
     boolean act(XAction[] actions);
 
     /**
      * 触发一系列动作，从而尝试让状态机进入下一个状态。
      * @param actions 动作链
      */
     boolean act(List<XAction> actions);
 
     /**
      * 获取当前状态。
      * @return 返回当前状态，有可能为null。
      */
 	String getCurrentState();
 
     /**
      * 注册监听
      * @param listener
      */
     void registerListener(Listener listener);
 
     /**
      * 取消注册监听
      * @param listener
      */
     void unregisterListener(Listener listener);
 }
