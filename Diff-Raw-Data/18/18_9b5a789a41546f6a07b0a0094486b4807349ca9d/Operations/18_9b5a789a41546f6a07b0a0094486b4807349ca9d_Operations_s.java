 /*
  * The MIT License
  *
  * Copyright (c) 2010 Petar Petrov
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.myglob;
 
 import android.content.Context;
 
 public enum Operations {
 	CHECK_ALL(0, R.string.operation_check_all),
 	CHECK_CURRENT_BALANCE(10, R.string.operation_check_balance),
 	CHECK_AVAIL_MINUTES(12, R.string.operation_check_avail_minutes),
 	CHECK_AVAIL_DATA(14, R.string.operation_check_avail_data),
 	CHECK_SMS_PACKAGE(16, R.string.operation_check_sms_pack),
 	CHECK_CREDIT_LIMIT(18, R.string.operation_check_credit_limit)
 	;
 
 	private int resId;
 	private int id;
 
 	Operations(int id, int resourceId) {
 		this.id = id;
 		this.resId = resourceId;
 	}
 
 	public String getText(Context context) {
 		return context.getString(this.resId);
 	}
 	
 	public int getId() {
 		return this.id;
 	}
 	
 	public int getResourceId() {
 		return this.resId;
 	}
 }
