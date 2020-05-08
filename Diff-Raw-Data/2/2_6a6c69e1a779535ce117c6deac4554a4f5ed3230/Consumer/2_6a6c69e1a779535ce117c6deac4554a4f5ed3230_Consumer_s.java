 package com.himanshu.waitnotify;
 
 public class Consumer extends Thread {
 	@Override
 	public void run() {
 		System.out.println(getName() + " - Starting consumer thread "+getName());
 		while (true) {
 			synchronized (SharedProducerConsumerResource.getInstance()) {
 				//We have got some data to process
 				while (SharedProducerConsumerResource.getInstance().size() == 0) {
					System.out.println(getName() + " - Consumer is FULL!!!");
 					try {
 						SharedProducerConsumerResource.getInstance().wait();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				String item = SharedProducerConsumerResource.getInstance().remove(0);
 				System.out.println(getName()+" - Item consumed : "+item);
 				SharedProducerConsumerResource.getInstance().notifyAll();
 			}
 		}
 	}
 }
