 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.duplications.statement;
 
 import org.sonar.duplications.DuplicationsException;
 import org.sonar.duplications.token.Token;
 import org.sonar.duplications.token.TokenQueue;
 
 import java.util.List;
 
 public class StatementChannelDisptacher {
 
   private final StatementChannel[] channels;
 
   public StatementChannelDisptacher(List<StatementChannel> channels) {
    this.channels = channels.toArray(new StatementChannel[channels.size()]);
   }
 
   public boolean consume(TokenQueue tokenQueue, List<Statement> statements) {
     Token nextToken = tokenQueue.peek();
     while (nextToken != null) {
       boolean channelConsumed = false;
       for (StatementChannel channel : channels) {
         if (channel.consume(tokenQueue, statements)) {
           channelConsumed = true;
           break;
         }
       }
       if (!channelConsumed) {
         throw new DuplicationsException("None of the statement channel has been able to consume token '" + tokenQueue.peek());
       }
       nextToken = tokenQueue.peek();
     }
     return true;
   }
 }
